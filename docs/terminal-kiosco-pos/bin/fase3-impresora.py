#!/usr/bin/env python3
r"""Prueba la Epson TM-T20IV-SP por ESC/POS directo, sin CUPS.

    sudo python3 fase3-impresora.py                 # estado + ticket de prueba
    sudo python3 fase3-impresora.py --solo-estado   # no gasta papel
    sudo python3 fase3-impresora.py --gaveta        # ademas dispara la gaveta

CUPS fue purgado en la Fase 2: no hay lpstat, ni lpr, ni colas. A esta
impresora se le habla escribiendo bytes ESC/POS crudos a /dev/usb/lp0, que
crea el modulo usblp.

El nodo es root:lp 660, asi que quien imprima tiene que estar en el grupo lp.
"caja1" esta en "lpadmin", que NO es lo mismo: lpadmin solo servia para
administrar colas de CUPS y CUPS ya no existe.

La impresora ademas CONTESTA. Los comandos DLE EOT n son de tiempo real: se
responden aunque haya trabajo en curso, y con ellos se sabe si hay papel, si
la tapa esta abierta y si el conector de la gaveta tiene algo enchufado. Eso
convierte "no imprime" en un diagnostico con causa, en vez de una conjetura.
"""
import grp
import os
import pwd
import select
import sys
import time

NODO = "/dev/usb/lp0"

BOLD = "\033[1m"
VERDE = "\033[32m"
ROJO = "\033[31m"
AMAR = "\033[33m"
FIN = "\033[0m"

# --- ESC/POS -----------------------------------------------------------
INIT      = b"\x1b\x40"           # ESC @   reinicia, deja un estado conocido
CP1252    = b"\x1b\x74\x10"       # ESC t 16  pagina de codigos WPC1252
NEGRITA   = b"\x1b\x45\x01"
NORMAL    = b"\x1b\x45\x00"
CENTRO    = b"\x1b\x61\x01"
IZQDA     = b"\x1b\x61\x00"
DOBLE     = b"\x1d\x21\x11"       # GS ! ancho+alto doble
SENCILLO  = b"\x1d\x21\x00"
CORTE     = b"\x1d\x56\x42\x00"   # GS V B 0  corte parcial con avance
GAVETA    = b"\x1b\x70\x00\x19\xfa"   # ESC p 0 25 250  pulso en el pin 2

# DLE EOT n -- estado en tiempo real
ESTADOS = [
    (1, "impresora"),
    (2, "offline"),
    (3, "error"),
    (4, "papel"),
]


def ok(txt):
    print("   %sOK%s   %s" % (VERDE, FIN, txt))


def mal(txt):
    print("   %s--%s   %s" % (ROJO, FIN, txt))


def avisa(txt):
    print("   %s!!%s   %s" % (AMAR, FIN, txt))


# ------------------------------------------------------------ 1. permisos
def revisa_nodo():
    print()
    print("%s== 1/4  El nodo y quien puede escribir en el%s" % (BOLD, FIN))
    print()
    if not os.path.exists(NODO):
        mal("no existe %s" % NODO)
        print("        La impresora no esta enchufada, o no cargo el modulo usblp.")
        print("        Comprueba:  lsusb | grep -i epson   y   lsmod | grep usblp")
        return False

    st = os.stat(NODO)
    duenio = pwd.getpwuid(st.st_uid).pw_name
    grupo = grp.getgrgid(st.st_gid).gr_name
    ok("%s existe  ->  %s:%s %o" % (NODO, duenio, grupo, st.st_mode & 0o777))

    # Quien es el usuario real, no el root del sudo.
    real = os.environ.get("SUDO_USER") or pwd.getpwuid(os.getuid()).pw_name
    for quien in dict.fromkeys([real, "caja1"]):
        try:
            info = pwd.getpwnam(quien)
        except KeyError:
            continue
        grupos = [g.gr_name for g in grp.getgrall() if quien in g.gr_mem]
        grupos.append(grp.getgrgid(info.pw_gid).gr_name)
        if grupo in grupos:
            ok("%-10s esta en '%s' -> puede escribir sin sudo" % (quien, grupo))
        else:
            mal("%-10s NO esta en '%s'" % (quien, grupo))
            if "lpadmin" in grupos:
                print("        (esta en 'lpadmin', que NO sirve: era de CUPS y CUPS ya no esta)")
            print("        Arreglo:  sudo usermod -aG %s %s" % (grupo, quien))
            print("        Hay que volver a iniciar sesion para que tome efecto.")
    return True


# --------------------------------------------------------------- 2. estado
def bits(b):
    return "".join("1" if b & (1 << i) else "0" for i in range(7, -1, -1))


def pregunta_estado(fd, n):
    """DLE EOT n. Devuelve el byte de respuesta o None si no contesta."""
    try:
        os.write(fd, bytes([0x10, 0x04, n]))
    except OSError as e:
        return None
    fin = time.time() + 1.0
    while time.time() < fin:
        listo, _, _ = select.select([fd], [], [], 0.2)
        if listo:
            try:
                dato = os.read(fd, 8)
            except OSError:
                return None
            if dato:
                return dato[-1]
    return None


def lee_estado(fd):
    print()
    print("%s== 2/4  Estado en tiempo real (DLE EOT)%s" % (BOLD, FIN))
    print("        La impresora contesta a esto aunque este ocupada.")
    print()
    respuestas = {}
    for n, nombre in ESTADOS:
        b = pregunta_estado(fd, n)
        respuestas[n] = b
        if b is None:
            mal("DLE EOT %d (%-9s) sin respuesta" % (n, nombre))
        else:
            ok("DLE EOT %d (%-9s) 0x%02x  %s" % (n, nombre, b, bits(b)))

    if all(v is None for v in respuestas.values()):
        avisa("No contesta ninguna consulta. Puede que el kernel no exponga la")
        print("        lectura del nodo. No es fatal: imprimir suele funcionar igual.")
        return respuestas

    print()
    # --- n=1 impresora: bit 2 = pin 3 del conector de gaveta
    b1 = respuestas.get(1)
    if b1 is not None:
        if b1 & 0x04:
            ok("gaveta: pin 3 en ALTO -> hay una gaveta conectada y CERRADA")
        else:
            avisa("gaveta: pin 3 en BAJO -> o esta abierta, o no hay gaveta conectada")

    # --- n=2 offline: bit 2 tapa, bit 5 falta papel, bit 6 error
    b2 = respuestas.get(2)
    if b2 is not None:
        if b2 & 0x04:
            mal("LA TAPA ESTA ABIERTA -> cierrala o no imprime")
        else:
            ok("tapa cerrada")
        if b2 & 0x20:
            mal("SIN PAPEL (parada por fin de papel)")
        if b2 & 0x40:
            mal("la impresora reporta ERROR (mira el LED)")

    # --- n=4 papel: bits 5,6 fin de papel; bits 2,3 papel por acabarse
    b4 = respuestas.get(4)
    if b4 is not None:
        if b4 & 0x60:
            mal("sensor de papel: SIN PAPEL")
        elif b4 & 0x0c:
            avisa("sensor de papel: queda poco, se va a acabar")
        else:
            ok("papel: hay suficiente")
    return respuestas


# --------------------------------------------------------------- 3. ticket
def ticket():
    """Un ticket corto que ademas prueba los acentos, que es donde falla."""
    t = bytearray()
    t += INIT
    t += CP1252
    t += CENTRO + NEGRITA + DOBLE
    t += b"PRUEBA POS\n"
    t += SENCILLO + NORMAL
    t += b"Landi CX20 - caja1-samuel\n"
    t += ("%s\n" % time.strftime("%Y-%m-%d %H:%M:%S")).encode("ascii")
    t += IZQDA
    t += b"-" * 42 + b"\n"
    # Si esta linea sale con simbolos raros, la pagina de codigos no es la
    # correcta y hay que cambiar el ESC t de arriba.
    t += "Acentos: años niño jamón ¿qué? ¡olé!\n".encode("cp1252", "replace")
    t += b"ASCII  : 0123456789 ABCDEFGHIJ\n"
    t += b"-" * 42 + b"\n"
    t += b"Bascula Torrey ... OK ('P' pelado)\n"
    t += b"Impresora ESC/POS ... esta hoja\n"
    t += b"\n\n"
    t += CORTE
    return bytes(t)


def imprime(fd):
    print()
    print("%s== 3/4  Ticket de prueba%s" % (BOLD, FIN))
    print()
    datos = ticket()
    try:
        escritos = os.write(fd, datos)
    except OSError as e:
        mal("no se pudo escribir: %s" % e)
        return False
    ok("%d bytes enviados a %s" % (escritos, NODO))
    print("        Mira el papel. Debe salir un ticket con la fecha, cortado.")
    print("        Fijate en la linea de acentos: si sale con basura, hay que")
    print("        cambiar la pagina de codigos (ESC t) en el backend.")
    return True


# --------------------------------------------------------------- 4. gaveta
def abre_gaveta(fd):
    print()
    print("%s== 4/4  Gaveta de dinero%s" % (BOLD, FIN))
    print()
    print("        Pulso ESC p 0 25 250 por el pin 2 del conector RJ11.")
    try:
        os.write(fd, GAVETA)
    except OSError as e:
        mal("no se pudo escribir: %s" % e)
        return False
    ok("pulso enviado")
    print("        Si la gaveta esta conectada, tiene que sonar el solenoide.")
    print("        Si no suena y el pin 3 salia BAJO en el paso 2, lo mas")
    print("        probable es que no haya gaveta enchufada al conector.")
    return True


def main():
    args = sys.argv[1:]
    solo_estado = "--solo-estado" in args
    con_gaveta = "--gaveta" in args

    print()
    print("%sImpresora Epson TM-T20IV-SP - ESC/POS directo, sin CUPS%s" % (BOLD, FIN))

    if not revisa_nodo():
        return 2

    try:
        fd = os.open(NODO, os.O_RDWR | os.O_NONBLOCK)
    except OSError as e:
        # Solo lectura no sirve para nada aqui; si falla RDWR probamos WRONLY
        # por si el kernel no deja abrir el nodo en bidireccional.
        try:
            fd = os.open(NODO, os.O_WRONLY | os.O_NONBLOCK)
            avisa("abierto solo para escritura (%s): no se podra leer el estado" % e)
        except OSError as e2:
            mal("no se pudo abrir %s: %s" % (NODO, e2))
            print("        Si es 'Permission denied', corre con sudo o entra al grupo lp.")
            return 2

    try:
        lee_estado(fd)
        if not solo_estado:
            imprime(fd)
        else:
            print()
            print("   (--solo-estado: no se imprime nada)")
        if con_gaveta:
            abre_gaveta(fd)
        else:
            print()
            print("   (la gaveta no se dispara sin --gaveta)")
    finally:
        os.close(fd)

    print()
    print("%s== Que sigue%s" % (BOLD, FIN))
    print("        Si el ticket salio bien, la impresora ya esta lista para el")
    print("        backend Node: son estos mismos bytes por %s." % NODO)
    print("        Falta meter a caja1 en el grupo 'lp' para que imprima sin sudo.")
    print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
