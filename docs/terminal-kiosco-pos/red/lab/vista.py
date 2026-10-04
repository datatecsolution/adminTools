#!/usr/bin/env python3
"""% de píxeles de una captura PNG (RGB/RGBA, 8 bits) parecidos a un color: vista.py captura.png R,G,B
Sin dependencias: decodifica el PNG con zlib (solo lo que produce el screendump de QEMU)."""
import struct
import sys
import zlib


def pixeles(ruta):
    datos = open(ruta, "rb").read()
    assert datos[:8] == b"\x89PNG\r\n\x1a\n", "no es PNG"
    i, idat, ancho, alto, tipo = 8, b"", 0, 0, 0
    while i < len(datos):
        largo, = struct.unpack(">I", datos[i:i + 4]); bloque = datos[i + 4:i + 8]; cont = datos[i + 8:i + 8 + largo]
        if bloque == b"IHDR":
            ancho, alto, prof, tipo = struct.unpack(">IIBB", cont[:10]); assert prof == 8
        elif bloque == b"IDAT":
            idat += cont
        i += 12 + largo
    bpp = {2: 3, 6: 4}[tipo]
    crudo = zlib.decompress(idat); paso = ancho * bpp; previa = bytearray(paso); pos = 0
    for _ in range(alto):
        filtro = crudo[pos]; fila = bytearray(crudo[pos + 1:pos + 1 + paso]); pos += 1 + paso
        for x in range(paso):
            a = fila[x - bpp] if x >= bpp else 0; b = previa[x]; c = previa[x - bpp] if x >= bpp else 0
            if filtro == 1: fila[x] = (fila[x] + a) & 255
            elif filtro == 2: fila[x] = (fila[x] + b) & 255
            elif filtro == 3: fila[x] = (fila[x] + (a + b) // 2) & 255
            elif filtro == 4:
                p = a + b - c; pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                fila[x] = (fila[x] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        for x in range(0, paso, bpp * 8):   # muestra 1 de cada 8 píxeles
            yield fila[x], fila[x + 1], fila[x + 2]
        previa = fila


r, g, b = map(int, sys.argv[2].split(","))
total = parecidos = 0
for pr, pg, pb in pixeles(sys.argv[1]):
    total += 1
    parecidos += abs(pr - r) < 25 and abs(pg - g) < 25 and abs(pb - b) < 25
print("%.0f" % (100.0 * parecidos / max(total, 1)))
