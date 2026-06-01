#!/usr/bin/env python3
"""
US-051 — Genera las plantillas Excel de carga inicial (productos y clientes)
para que el cliente (Urbina) llene su catálogo. Cada plantilla trae:
  - hoja de Instrucciones,
  - hoja de datos con encabezados, filas de ejemplo y validaciones (dropdowns),
  - hoja oculta "Listas" con los valores válidos (categorías, impuestos).

Las columnas se alinean con el contrato del sistema:
  ProductRequest{name, price, categoryId(=categoría), taxId(=impuesto), altCode}
  CustomerCreateRequest{name, rtn, address, phone}

Listas (categorías/impuestos) = snapshot de admin_tools al 2026-06-01.
Regenerar:  python3 docs/plantillas/generar_plantillas.py
"""
import os
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.worksheet.datavalidation import DataValidation
from openpyxl.utils import get_column_letter

OUT_DIR = os.path.dirname(os.path.abspath(__file__))

# --- Snapshot de catálogos (admin_tools) ---
CATEGORIAS = [
    "ACCES", "BEBE", "BEBID", "BOLSO", "CABELLO", "COSTURA", "CUIDADO PERSONAL",
    "DECOR", "ELECT", "ELECTRONICA", "FERRE", "FIEST", "GOLOS", "HOGAR", "JOYERIA",
    "JUGUE", "MEDIC", "OTROS", "REGAL", "RODA", "ROPA", "SEGUR", "SERVI", "UTIL",
    "Varios", "ZAPAT",
]
IMPUESTOS = ["Exectos (0%)", "Basicos (15%)", "Lujo (18%)"]

HEADER_FILL = PatternFill("solid", fgColor="1F4E78")
HEADER_FONT = Font(bold=True, color="FFFFFF", size=11)
REQ_FONT = Font(bold=True, color="FFFFFF", size=11)
EXAMPLE_FONT = Font(italic=True, color="888888")
TITLE_FONT = Font(bold=True, size=14, color="1F4E78")
THIN = Side(style="thin", color="BFBFBF")
BORDER = Border(left=THIN, right=THIN, top=THIN, bottom=THIN)


def estilizar_encabezado(ws, ncols, fila=1):
    for c in range(1, ncols + 1):
        cell = ws.cell(row=fila, column=c)
        cell.fill = HEADER_FILL
        cell.font = HEADER_FONT
        cell.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
        cell.border = BORDER
    ws.row_dimensions[fila].height = 28
    ws.freeze_panes = ws.cell(row=fila + 1, column=1)


def hoja_listas(wb, categorias, impuestos):
    ws = wb.create_sheet("Listas")
    ws["A1"] = "Categorias"
    ws["B1"] = "Impuestos"
    for i, v in enumerate(categorias, start=2):
        ws.cell(row=i, column=1, value=v)
    for i, v in enumerate(impuestos, start=2):
        ws.cell(row=i, column=2, value=v)
    ws.sheet_state = "hidden"
    return ws


def hoja_instrucciones(wb, titulo, lineas):
    ws = wb.create_sheet("Instrucciones", 0)
    ws["A1"] = titulo
    ws["A1"].font = TITLE_FONT
    ws.column_dimensions["A"].width = 110
    r = 3
    for ln in lineas:
        c = ws.cell(row=r, column=1, value=ln)
        c.alignment = Alignment(wrap_text=True, vertical="top")
        if ln.endswith(":") or ln.startswith("•") is False and ln.isupper():
            c.font = Font(bold=True)
        r += 1
    return ws


def dv(formula, prompt, allow_blank=True):
    d = DataValidation(type="list", formula1=formula, allow_blank=allow_blank, showDropDown=False)
    d.error = "Elegí un valor de la lista."
    d.errorTitle = "Valor inválido"
    d.prompt = prompt
    d.promptTitle = "Ayuda"
    return d


# ============================ PRODUCTOS ============================
def plantilla_productos():
    wb = Workbook()
    wb.remove(wb.active)
    hoja_listas(wb, CATEGORIAS, IMPUESTOS)

    ws = wb.create_sheet("Productos")
    headers = [
        ("Nombre *", 34),
        ("Categoria *", 20),
        ("Impuesto (ISV) *", 16),
        ("Precio de venta *", 16),
        ("Codigo de barras", 20),
        ("Codigo alterno", 16),
    ]
    for i, (h, w) in enumerate(headers, start=1):
        ws.cell(row=1, column=i, value=h)
        ws.column_dimensions[get_column_letter(i)].width = w
    estilizar_encabezado(ws, len(headers))

    ejemplos = [
        ["Coca Cola 600ml", "BEBID", "Basicos (15%)", 25.00, "7501055300013", 1001],
        ["Camiseta algodon M", "ROPA", "Basicos (15%)", 180.00, "", 1002],
        ["Medicamento generico", "MEDIC", "Exectos (0%)", 45.50, "", ""],
    ]
    for r, fila in enumerate(ejemplos, start=2):
        for c, val in enumerate(fila, start=1):
            cell = ws.cell(row=r, column=c, value=val)
            cell.font = EXAMPLE_FONT
            cell.border = BORDER

    n = 600
    last = 1 + len(ejemplos) + n
    dv_cat = dv("=Listas!$A$2:$A$%d" % (1 + len(CATEGORIAS)), "Seleccioná la categoría del producto.")
    dv_imp = dv("=Listas!$B$2:$B$%d" % (1 + len(IMPUESTOS)), "Seleccioná el ISV que aplica.")
    ws.add_data_validation(dv_cat)
    ws.add_data_validation(dv_imp)
    dv_cat.add("B2:B%d" % last)
    dv_imp.add("C2:C%d" % last)

    dv_precio = DataValidation(type="decimal", operator="greaterThanOrEqual", formula1="0", allow_blank=True)
    dv_precio.error = "El precio debe ser un número mayor o igual a 0."
    dv_precio.errorTitle = "Precio inválido"
    ws.add_data_validation(dv_precio)
    dv_precio.add("D2:D%d" % last)

    instr = [
        "PLANTILLA DE CARGA INICIAL — PRODUCTOS",
        "",
        "Llená la hoja \"Productos\" (una fila por producto). Las filas en gris claro son EJEMPLOS: borralas o reemplazalas.",
        "",
        "COLUMNAS:",
        "• Nombre *  (obligatorio): nombre del producto tal como aparecerá en facturas y catálogo.",
        "• Categoria *  (obligatorio): elegí una de la lista desplegable. Si falta una categoría, avisanos para agregarla.",
        "• Impuesto (ISV) *  (obligatorio): Exectos (0%) = exonerado, Basicos (15%), Lujo (18%). Elegí de la lista.",
        "• Precio de venta *  (obligatorio): precio al público, en Lempiras, CON impuesto incluido. Solo números (ej. 25.00).",
        "• Codigo de barras  (opcional): si el producto tiene código de barras escaneable.",
        "• Codigo alterno  (opcional): código interno/SKU si manejás uno.",
        "",
        "REGLAS:",
        "• No cambies los encabezados ni el orden de las columnas.",
        "• No dejes filas vacías en medio de los datos.",
        "• Categoría e Impuesto deben venir SIEMPRE de la lista desplegable (validación activada).",
        "• El precio incluye el ISV (el sistema calcula el desglose automáticamente).",
        "",
        "Cuando termines, guardá el archivo y envialo para la carga. Soporta varios cientos de productos por archivo.",
    ]
    hoja_instrucciones(wb, "Instrucciones — Productos", instr)

    path = os.path.join(OUT_DIR, "plantilla_productos.xlsx")
    wb.save(path)
    return path


# ============================ CLIENTES ============================
def plantilla_clientes():
    wb = Workbook()
    wb.remove(wb.active)

    ws = wb.create_sheet("Clientes")
    headers = [
        ("Nombre *", 34),
        ("RTN", 20),
        ("Direccion", 40),
        ("Telefono", 18),
    ]
    for i, (h, w) in enumerate(headers, start=1):
        ws.cell(row=1, column=i, value=h)
        ws.column_dimensions[get_column_letter(i)].width = w
    estilizar_encabezado(ws, len(headers))

    ejemplos = [
        ["Consumidor Final", "", "", ""],
        ["Distribuidora La Económica", "08011985123456", "Bo. El Centro, La Ceiba", "2443-1122"],
        ["María José Mayorga", "", "Col. Las Flores, calle 3", "9988-7766"],
    ]
    for r, fila in enumerate(ejemplos, start=2):
        for c, val in enumerate(fila, start=1):
            cell = ws.cell(row=r, column=c, value=val)
            cell.font = EXAMPLE_FONT
            cell.border = BORDER
            if c == 2:  # RTN como texto para no perder ceros
                cell.number_format = "@"

    n = 600
    last = 1 + len(ejemplos) + n
    # RTN: 14 dígitos exactos (o vacío). Validación por longitud de texto.
    dv_rtn = DataValidation(type="textLength", operator="equal", formula1="14", allow_blank=True)
    dv_rtn.error = "El RTN hondureño debe tener exactamente 14 dígitos (o dejarlo vacío)."
    dv_rtn.errorTitle = "RTN inválido"
    dv_rtn.prompt = "14 dígitos, sin guiones. Dejalo vacío para Consumidor Final."
    dv_rtn.promptTitle = "RTN"
    ws.add_data_validation(dv_rtn)
    dv_rtn.add("B2:B%d" % last)
    for r in range(2, last + 1):
        ws.cell(row=r, column=2).number_format = "@"

    instr = [
        "PLANTILLA DE CARGA INICIAL — CLIENTES",
        "",
        "Llená la hoja \"Clientes\" (una fila por cliente). Las filas en gris claro son EJEMPLOS: borralas o reemplazalas.",
        "",
        "COLUMNAS:",
        "• Nombre *  (obligatorio): nombre o razón social del cliente.",
        "• RTN  (opcional): 14 dígitos, sin guiones. Dejalo vacío para 'Consumidor Final'. (validación de 14 dígitos activada)",
        "• Direccion  (opcional).",
        "• Telefono  (opcional).",
        "",
        "REGLAS:",
        "• No cambies los encabezados ni el orden de las columnas.",
        "• No dejes filas vacías en medio de los datos.",
        "• El RTN debe tener 14 dígitos exactos o quedar vacío.",
        "",
        "Cuando termines, guardá el archivo y envialo para la carga.",
    ]
    hoja_instrucciones(wb, "Instrucciones — Clientes", instr)

    path = os.path.join(OUT_DIR, "plantilla_clientes.xlsx")
    wb.save(path)
    return path


if __name__ == "__main__":
    print("Generada:", plantilla_productos())
    print("Generada:", plantilla_clientes())
