#!/usr/bin/env python3
r"""Prueba una bascula Torrey L-PC / L-EQ con SU protocolo, no a ciegas.

    sudo python3 fase3-torrey.py /dev/ttyUSB0
    sudo python3 fase3-torrey.py /dev/ttyUSB0 --print 30

Segun el manual de la serie (FABATSA, "PC SERIAL COMMUNICATION"):
  - 9600 bps, 1 start / 8 datos / 0 paridad / 1 stop  -> 8N1
  - la bascula NO emite sola: hay que pedirle el peso
  - la PC manda el caracter 'P' en ASCII, PELADO (sin CR, sin LF)
  - la bascula contesta  PESO + CR
  - la tecla PRINT de la bascula tambien manda el peso por el puerto serie

El cable tiene que ser CRUZADO (null-modem). El manual dibuja:
    DB9 (PC)            DB9 (bascula)
      2 RXD  ---\  /---  2 RXD
      3 TXD  ---/  \---  3 TXD      (2 y 3 cruzados)
      4 DTR  ---\  /---  4 DTR
      6 DSR  ---/  \---  6 DSR      (4 y 6 cruzados)
      5 GND  ----------  5 GND      (directo)
Un cable serie DIRECTO deja TX contra TX y no se oye nada: silencio total,
que es exactamente lo que da un cable equivocado y no se distingue de una
bascula apagada mirando solo el trafico.
"""
import os
import sys
import time

AQUI = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, AQUI)

import importlib.util
_spec = importlib.util.spec_from_file_location(
    "fase3_bascula", os.path.join(AQUI, "fase3-bascula.py"))
_bas = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_bas)

abrir        = _bas.abrir
lineas_modem = _bas.lineas_modem
escuchar     = _bas.escuchar
volcado      = _bas.volcado

BOLD = "\033[1m"
VERDE = "\033[32m"
ROJO = "\033[31m"
FIN = "\033[0m"

BAUD = 9600

# 'P' pelado es el del manual. Las variantes van detras solo por si la
# unidad lleva firmware de otra tanda.
PETICIONES = [
    ("'P' pelado (el del manual)", b"P"),
    ("'P' + CR",                   b"P\r"),
    ("'p' minuscula",              b"p"),
    ("ENQ (0x05)",                 b"\x05"),
]


def ok(txt):
    print("   %sOK%s   %s" % (VERDE, FIN, txt))


def mal(txt):
    print("   %s--%s   %s" % (ROJO, FIN, txt))


def interpreta(buf):
    """La trama Torrey es texto: peso + CR. Lo confirmamos como tal."""
    txt = buf.decode("latin-1")
    limpio = txt.replace("\r", "").replace("\n", "").strip()
    tiene_digito = any(c.isdigit() for c in limpio)
    return limpio, tiene_digito


def pedir_peso(dev):
    print()
    print("%s== 1/2  Pidiendo el peso con el comando Torrey ('P' a 9600 8N1)%s"
          % (BOLD, FIN))
    print("        Pon algo de peso en el plato antes de seguir.")
    print()
    fd = abrir(dev, BAUD, 8, None)
    if fd is None:
        mal("no se pudo abrir %s a %d" % (dev, BAUD))
        return False
    # El cable del manual cruza DTR(4) con DSR(6): la bascula espera ver su
    # DSR activo. Levantamos DTR/RTS por si de ahi cuelga su habilitacion.
    lineas_modem(fd, dtr=True, rts=True)
    time.sleep(0.2)

    hubo = False
    try:
        for nombre, cmd in PETICIONES:
            import termios
            termios.tcflush(fd, termios.TCIOFLUSH)
            os.write(fd, cmd)
            buf = escuchar(fd, 1.5)
            if not buf:
                mal("%-28s sin respuesta" % nombre)
                continue
            limpio, tiene_digito = interpreta(buf)
            if tiene_digito:
                ok("%-28s RESPONDE: %r" % (nombre, limpio))
                print()
                volcado(buf)
                hubo = True
                break
            ok("%-28s contesta algo (sin cifras): %r" % (nombre, limpio))
            volcado(buf)
            hubo = True
            break
    finally:
        os.close(fd)
    return hubo


def escucha_print(dev, segundos):
    print()
    print("%s== 2/2  La tecla PRINT de la bascula%s" % (BOLD, FIN))
    print("        Segun el manual, PRINT manda el peso mostrado por el puerto")
    print("        serie. Esto no depende de que la PC acierte el comando:")
    print("        si el cable esta bien, aqui TIENE que llegar algo.")
    print()
    print("        >> Pon peso en el plato y pulsa PRINT varias veces")
    print("           durante los proximos %d segundos." % segundos)
    print()
    fd = abrir(dev, BAUD, 8, None)
    if fd is None:
        mal("no se pudo abrir %s a %d" % (dev, BAUD))
        return False
    lineas_modem(fd, dtr=True, rts=True)
    try:
        fin = time.time() + segundos
        total = b""
        while time.time() < fin:
            trozo = escuchar(fd, 1.0)
            if trozo:
                total += trozo
                limpio, _ = interpreta(trozo)
                ok("llego: %r" % limpio)
            else:
                queda = int(fin - time.time())
                sys.stdout.write("\r        escuchando... %2d s  " % max(queda, 0))
                sys.stdout.flush()
        sys.stdout.write("\r" + " " * 40 + "\r")
    finally:
        os.close(fd)
    if total:
        print()
        volcado(total)
        return True
    mal("nada durante los %d s, ni pulsando PRINT" % segundos)
    return False


def veredicto(respondio):
    print()
    print("%s== Que significa%s" % (BOLD, FIN))
    if respondio:
        ok("La bascula habla. Anota la trama de arriba: ese es el formato")
        print("        que hay que parsear en el POS (peso + CR).")
        return 0
    print("        Silencio con el protocolo correcto y el adaptador ya probado")
    print("        con loopback. Queda una sola causa fisica de peso:")
    print()
    print("        >> EL CABLE ES DIRECTO Y TIENE QUE SER CRUZADO (null-modem).")
    print()
    print("        El loopback del extremo lejano dio eco exacto, pero ese eco")
    print("        sale igual con cable directo que con cruzado: solo prueba")
    print("        continuidad de los pines 2 y 3, no su orientacion.")
    print()
    print("        Como confirmarlo en 1 minuto con un multimetro en continuidad:")
    print("           punta en el pin 2 de un extremo y en el pin 3 del otro")
    print("             pita  -> cable CRUZADO (correcto)")
    print("             no pita, pero pita 2 con 2  -> DIRECTO (hay que cambiarlo)")
    print()
    print("        La solucion barata es un adaptador null-modem DB9 M/H")
    print("        intercalado entre el CH340 y el cable actual.")
    print()
    print("        Antes de comprar nada, descarta lo gratis:")
    print("        - la bascula se apaga sola a los 15 min si va con bateria;")
    print("          conectala al eliminador de corriente.")
    print("        - que el cable vaya al puerto de datos, no al de impresora.")
    return 1


def main():
    dev = "/dev/ttyUSB0"
    segundos = 30
    args = sys.argv[1:]
    if args and args[0].startswith("/dev/"):
        dev = args.pop(0)
    solo_print = False
    if args and args[0] == "--print":
        solo_print = True
        args.pop(0)
        if args:
            segundos = int(args.pop(0))

    if not os.path.exists(dev):
        mal("no existe %s" % dev)
        return 2

    respondio = False
    if not solo_print:
        respondio = pedir_peso(dev)
    if not respondio:
        respondio = escucha_print(dev, segundos)
    return veredicto(respondio)


if __name__ == "__main__":
    sys.exit(main())
