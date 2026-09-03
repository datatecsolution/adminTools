#!/usr/bin/env python3
"""Sondea una bascula serie: primero escucha, y si calla le manda comandos.

    sudo python3 fase3-bascula.py /dev/ttyUSB0
    sudo python3 fase3-bascula.py /dev/ttyUSB0 --loopback
    sudo python3 fase3-bascula.py /dev/ttyUSB0 --lineas
    sudo python3 fase3-bascula.py /dev/ttyUSB0 --escuchar 30 --baudios 9600

Solo lee y (en la fase B) manda bytes de peticion inofensivos. No configura
nada de forma permanente.

Nota sobre la paridad: se abre con iflag=0, o sea INPCK apagado. El kernel
entonces NO valida la paridad y entrega los bytes igual aunque el formato no
coincida. Por eso la fase A (escucha) prueba solo velocidades: si a 8N1 hay
silencio, a 7E1 tambien lo habria. La paridad si cambia lo que ENTIENDE la
bascula, asi que la fase B (comandos) si recorre formatos.
"""
import os
import select
import sys
import termios
import time
import fcntl
import struct
import array

BAUDIOS = [9600, 4800, 2400, 1200, 19200, 38400, 57600, 115200]

# (nombre, bits de datos, paridad)  -- paridad: None / "E" / "O"
FORMATOS = [("8N1", 8, None), ("7E1", 7, "E"), ("7O1", 7, "O"), ("8E1", 8, "E")]

# Comandos de peticion habituales en basculas de mostrador.
COMANDOS = [
    # Torrey L-PC/L-EQ: un solo byte 0x50, SIN CR. Es el unico que entiende.
    # Iba abajo en la lista con CR y por eso el sondeo la daba por muda (Fase 3).
    ("P pelado",   b"P"),
    ("ENQ (0x05)", b"\x05"),
    ("W CR",       b"W\r"),
    ("P CR",       b"P\r"),
    ("S CR LF",    b"S\r\n"),
    ("ESC p",      b"\x1bp"),
    ("# CR",       b"#\r"),
    ("SI CR LF",   b"SI\r\n"),   # Mettler-Toledo MT-SICS: peso inmediato
    ("Z CR",       b"Z\r"),      # Cardinal / Detecto
    ("ESC W",      b"\x1bW"),
    ("SP CR",      b" \r"),
]

TIOCMGET = getattr(termios, "TIOCMGET", 0x5415)
TIOCMSET = getattr(termios, "TIOCMSET", 0x5418)
TIOCM_DTR = 0x002
TIOCM_RTS = 0x004
TIOCM_CTS = 0x020
TIOCM_CAR = 0x040          # DCD
TIOCM_RNG = 0x080          # RI
TIOCM_DSR = 0x100

# Lineas que las MANDA EL OTRO EXTREMO. Si alguna esta alta, hay algo vivo
# al final del cable: eso no lo puede fingir el software.
ENTRADAS = [("CTS", TIOCM_CTS), ("DSR", TIOCM_DSR),
            ("DCD", TIOCM_CAR), ("RI", TIOCM_RNG)]


def abrir(dev, baud, bits=8, paridad=None):
    """Abre el puerto en crudo, sin control de flujo, no bloqueante."""
    const = getattr(termios, "B%d" % baud, None)
    if const is None:
        return None
    fd = os.open(dev, os.O_RDWR | os.O_NOCTTY | os.O_NONBLOCK)
    cc = termios.tcgetattr(fd)[6]
    cc[termios.VMIN] = 0
    cc[termios.VTIME] = 0
    iflag = 0                                     # sin IXON/IXOFF/ICRNL/INPCK
    oflag = 0                                     # sin OPOST
    cflag = termios.CREAD | termios.CLOCAL        # ignora las lineas de modem
    cflag |= termios.CS7 if bits == 7 else termios.CS8
    if paridad:
        cflag |= termios.PARENB
        if paridad == "O":
            cflag |= termios.PARODD
    lflag = 0                                     # sin ICANON/ECHO
    termios.tcsetattr(fd, termios.TCSANOW,
                      [iflag, oflag, cflag, lflag, const, const, cc])
    termios.tcflush(fd, termios.TCIOFLUSH)
    return fd


def lineas_modem(fd, dtr=True, rts=True):
    """Fuerza DTR/RTS. Algunas basculas se alimentan o se habilitan por ahi."""
    try:
        buf = array.array('i', [0])
        fcntl.ioctl(fd, TIOCMGET, buf, True)
        estado = buf[0]
        estado = (estado | TIOCM_DTR) if dtr else (estado & ~TIOCM_DTR)
        estado = (estado | TIOCM_RTS) if rts else (estado & ~TIOCM_RTS)
        fcntl.ioctl(fd, TIOCMSET, struct.pack('i', estado))
        return True
    except OSError:
        return False


def escuchar(fd, segundos, tope=2048):
    """Lee hasta `segundos`. select() es lo que hace que la espera sea real."""
    fin = time.time() + segundos
    buf = b""
    while time.time() < fin and len(buf) < tope:
        listo, _, _ = select.select([fd], [], [], min(0.2, max(0.0, fin - time.time())))
        if listo:
            try:
                trozo = os.read(fd, 4096)
            except OSError:
                break
            if trozo:
                buf += trozo
    return buf


def volcado(buf, sangria="      "):
    """Hexdump corto + la version legible, que es donde se ve el peso."""
    lineas = []
    for i in range(0, min(len(buf), 128), 16):
        cacho = buf[i:i + 16]
        hexa = " ".join("%02x" % b for b in cacho)
        texto = "".join(chr(b) if 32 <= b < 127 else "." for b in cacho)
        lineas.append("%s%-47s  |%s|" % (sangria, hexa, texto))
    if len(buf) > 128:
        lineas.append("%s... (%d bytes en total)" % (sangria, len(buf)))
    legible = "".join(chr(b) if 32 <= b < 127 else
                      {10: "\\n", 13: "\\r", 2: "<STX>", 3: "<ETX>", 5: "<ENQ>"}
                      .get(b, ".") for b in buf[:200])
    lineas.append("%stexto: %s" % (sangria, legible))
    return "\n".join(lineas)


# ------------------------------------------------------------------- lineas
def leer_entradas(fd):
    buf = array.array('i', [0])
    fcntl.ioctl(fd, TIOCMGET, buf, True)
    return {nom: bool(buf[0] & bit) for nom, bit in ENTRADAS}


def pinta(est):
    return "  ".join("%s=%s" % (n, "ALTA" if est[n] else "baja")
                     for n, _ in ENTRADAS)


def lineas(dev, segundos=8):
    """Mira las lineas de control que manda el OTRO extremo. Cero hardware.

    No necesita puentes ni clips: solo lee el estado electrico de CTS/DSR/DCD.
    Si alguna esta ALTA, hay un aparato encendido y conectado al final del
    cable (aunque su TX este mudo o cruzado). Si todas estan bajas el test no
    concluye por si solo: hay basculas que no cablean ninguna de estas lineas.
    Por eso se compara con la bascula ENCHUFADA y DESENCHUFADA.
    """
    print("   Lineas de control en %s (no hace falta ningun puente)" % dev)
    fd = abrir(dev, 9600)
    if fd is None:
        print("   --   no se pudo abrir el puerto")
        return 1
    try:
        lineas_modem(fd, dtr=False, rts=False)
        time.sleep(0.3)
        sin = leer_entradas(fd)
        print("   -- con DTR/RTS en reposo :  %s" % pinta(sin))

        lineas_modem(fd, dtr=True, rts=True)
        time.sleep(0.3)
        con = leer_entradas(fd)
        print("   -- con DTR/RTS activados :  %s" % pinta(con))

        print("   -- vigilando %d s por si alguna cambia..." % segundos)
        base = dict(con)
        cambios = 0
        fin = time.time() + segundos
        while time.time() < fin:
            time.sleep(0.2)
            ahora = leer_entradas(fd)
            for nom, _ in ENTRADAS:
                if ahora[nom] != base[nom]:
                    print("      %s: %s -> %s" % (nom,
                          "ALTA" if base[nom] else "baja",
                          "ALTA" if ahora[nom] else "baja"))
                    base[nom] = ahora[nom]
                    cambios += 1
    finally:
        os.close(fd)

    viva = [n for n, _ in ENTRADAS if con[n] or sin[n]]
    print()
    if viva:
        print("   OK   Hay senal en %s -> al final del cable hay un aparato" % ", ".join(viva))
        print("        ENCENDIDO y conectado. El adaptador y el cable conducen.")
        print("        Entonces el silencio en RX es una de estas dos:")
        print("          - TX/RX cruzados (hace falta cable/adaptador null-modem), o")
        print("          - la salida serie de la bascula esta apagada en su menu.")
    else:
        print("   --   Todas las entradas bajas. Repite AHORA con la bascula")
        print("        DESENCHUFADA del adaptador y compara:")
        print("          - si sale igual -> la bascula no cablea estas lineas y")
        print("            el test no decide; hace falta el loopback.")
        print("          - si al enchufarla alguna sube -> si hay vida en el cable.")
    if cambios:
        print("   (%d cambios de estado durante la vigilancia)" % cambios)
    return 0


# ------------------------------------------------------------------ loopback
def loopback(dev):
    """Puentea pines 2-3 del DB9 y corre esto. Separa dos causas distintas.

    Si vuelve el eco -> el adaptador y su cable USB estan bien, y el silencio
    es de la bascula (apagada, salida serie deshabilitada, o cable equivocado).
    Si NO vuelve      -> el problema esta antes: adaptador, driver o cable.
    """
    print("   Prueba de LOOPBACK en %s" % dev)
    print("   Puentea los pines 2 y 3 del DB9 (un clip basta) y no toques nada mas.")
    print()
    prueba = b"LOOPBACK-1234567890\r\n"
    fallos = 0
    for baud in (9600, 115200):
        fd = abrir(dev, baud)
        if fd is None:
            continue
        try:
            lineas_modem(fd)
            termios.tcflush(fd, termios.TCIOFLUSH)
            os.write(fd, prueba)
            eco = escuchar(fd, 1.5, tope=256)
        finally:
            os.close(fd)
        if eco == prueba:
            print("   OK   %-6d  eco exacto (%d bytes)" % (baud, len(eco)))
        elif prueba in eco:
            # La grapa rebota al apoyarla: cada micro-corte del hilo RX entra
            # como 0x00. Si el texto completo esta ahi dentro, el adaptador
            # SI hace eco; lo que falla es el contacto del puente, no el CH340.
            print("   OK   %-6d  eco correcto, con ruido de contacto "
                  "(%d bytes; sujeta mejor la grapa)" % (baud, len(eco)))
            print(volcado(eco))
        elif eco:
            print("   >>   %-6d  vuelve algo pero distinto:" % baud)
            print(volcado(eco))
            fallos += 1
        else:
            print("   --   %-6d  no vuelve nada" % baud)
            fallos += 1

    print()
    if fallos == 0:
        print("   OK   El adaptador CH340 transmite y recibe bien.")
        print("        Entonces el silencio es de la bascula: revisa su menu de")
        print("        configuracion (salida serie / RS232 / 'print mode'), que")
        print("        este encendida, y que el cable vaya a su puerto de datos.")
        print("        Si el cable es directo, prueba uno cruzado (null-modem):")
        print("        adaptador y bascula suelen ser ambos DTE y hay que cruzar 2-3.")
    else:
        print("   --   Sin eco. Con los pines 2-3 puenteados esto no deberia pasar:")
        print("        - esta bien hecho el puente? (pin 2 = RXD, pin 3 = TXD)")
        print("        - el adaptador CH340 puede estar defectuoso")
        print("        - si el 'DB9' es en realidad un RJ45/RJ11 de la bascula, el")
        print("          puente hay que hacerlo en el lado DB9 del adaptador")
    return 0 if fallos == 0 else 1


# ------------------------------------------------------------- escucha larga
def escucha_larga(dev, segundos, baud):
    """Escucha un rato largo a una sola velocidad, para mover el plato.

    Hay basculas que no emiten en reposo y solo mandan la trama cuando el peso
    se estabiliza en un valor distinto de cero.
    """
    print("   Escucha larga: %d s a %d baudios" % (segundos, baud))
    print("   AHORA: pon y quita peso del plato varias veces.")
    fd = abrir(dev, baud)
    if fd is None:
        print("   --   velocidad no soportada")
        return 1
    try:
        lineas_modem(fd)
        buf = escuchar(fd, segundos, tope=8192)
    finally:
        os.close(fd)
    if buf:
        print("   >> \033[1mHAY DATOS (%d bytes)\033[0m" % len(buf))
        print(volcado(buf))
        return 0
    print("   --   silencio durante los %d s, con peso incluido" % segundos)
    return 1


def main():
    args = sys.argv[1:]
    dev = "/dev/ttyUSB0"
    modo = "sonda"
    segundos = 30
    baud_largo = 9600
    i = 0
    while i < len(args):
        a = args[i]
        if a == "--loopback":
            modo = "loopback"
        elif a == "--lineas":
            modo = "lineas"
        elif a == "--escuchar":
            modo = "largo"
            i += 1
            segundos = int(args[i])
        elif a == "--baudios":
            i += 1
            baud_largo = int(args[i])
        elif not a.startswith("-"):
            dev = a
        i += 1

    if not os.path.exists(dev):
        print("   --   %s no existe. Esta enchufada y encendida la bascula?" % dev)
        return 1

    if modo == "loopback":
        return loopback(dev)
    if modo == "lineas":
        return lineas(dev)
    if modo == "largo":
        return escucha_larga(dev, segundos, baud_largo)

    print("   Fase A - escucha pasiva (streaming), 3 s por velocidad")
    print("   (la paridad no se recorre aqui: INPCK esta apagado, los bytes")
    print("    llegan igual aunque el formato no coincida)")
    encontrado = []
    for baud in BAUDIOS:
        try:
            fd = abrir(dev, baud)
        except OSError as e:
            print("   --   %-6d  (no se pudo abrir: %s)" % (baud, e))
            continue
        if fd is None:
            continue
        try:
            lineas_modem(fd)
            buf = escuchar(fd, 3)
        finally:
            os.close(fd)
        if buf:
            print("   >> \033[1m%d baudios: HAY DATOS (%d bytes)\033[0m" % (baud, len(buf)))
            print(volcado(buf))
            encontrado.append(baud)
        else:
            print("   --   %-6d  (silencio)" % baud)

    if encontrado:
        print()
        print("   OK   La bascula emite en continuo a %s baudios."
              % " o ".join(str(b) for b in encontrado))
        print("        Si aparecen varias, la buena es la que se lee como numeros.")
        return 0

    print()
    print("   Fase B - la bascula calla; probamos comandos de peticion")
    for baud in (9600, 4800, 2400, 19200):
        for fnombre, bits, par in FORMATOS:
            try:
                fd = abrir(dev, baud, bits, par)
            except OSError:
                continue
            if fd is None:
                continue
            hubo = False
            try:
                lineas_modem(fd)
                for nombre, cmd in COMANDOS:
                    termios.tcflush(fd, termios.TCIOFLUSH)
                    try:
                        os.write(fd, cmd)
                    except OSError:
                        continue
                    buf = escuchar(fd, 1.0, tope=256)
                    if buf:
                        print("   >> \033[1m%d %s + %s: RESPONDE\033[0m"
                              % (baud, fnombre, nombre))
                        print(volcado(buf))
                        hubo = True
            finally:
                os.close(fd)
            if hubo:
                return 0
        print("   --   %-6d  (no contesta a ningun comando, en 8N1/7E1/7O1/8E1)" % baud)

    print()
    print("   --   Sin respuesta a nada. El siguiente paso NO es probar mas")
    print("        velocidades: es descartar el cable con la prueba de loopback.")
    print()
    print("          sudo python3 %s %s --loopback" % (sys.argv[0], dev))
    print()
    print("        Y si el adaptador da eco, entonces revisar en la bascula:")
    print("        - esta encendida?")
    print("        - el cable va al puerto de DATOS (no al de la impresora)?")
    print("        - muchas basculas traen la salida serie APAGADA de fabrica y")
    print("          hay que habilitarla desde su menu de configuracion.")
    print("        - hay basculas que solo emiten con peso estable != 0:")
    print("            sudo python3 %s %s --escuchar 30" % (sys.argv[0], dev))
    return 1


if __name__ == "__main__":
    sys.exit(main())
