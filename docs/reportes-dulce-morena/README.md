# Reportes de ventas — Dulce Morena

Análisis exploratorio hecho el 2026-07-18/19 sobre datos reales del cliente **Dulce Morena**
(panadería/pastelería/cafetería, Honduras, L). Se guarda aquí porque estos reportes son la
**base de un feature de reportería del POS** (queries, agrupaciones y métricas ya validadas
contra datos reales).

## Contenido

| Ruta | Qué es |
|---|---|
| `sql/` | Las consultas tal como se ejecutaron |
| `datos/` | Resultados exportados en TSV (snapshot al 2026-07-18) |
| `salida/gen_excel.py` | Generador del Excel (openpyxl, con estilos y hoja de proyección) |
| `salida/reporte_ventas_dulce_morena.xlsx` | Entregable final, 3 hojas |

## Origen de los datos

Stack de pruebas dockerizado con datos reales del cliente, en el servidor de Ronal
(ver memoria `project_pruebas_dulce_ronal`):

- Host: `ronal@201.190.38.238` (o `10.10.0.1` por WireGuard), ruta `/home/ronal/pruebas-dulce/stack`
- Container MySQL: `mysql-pruebas-dulce` (password en `.env` → `PRUEBAS_MYSQL_ROOT_PASSWORD`)
- BDs: `admin_tools` (catálogo común) + `admin_tools_caja_1..4`. **Solo cajas 1, 3 y 4 tienen ventas.**
- Producción del cliente: MySQL en `192.168.1.23`

Patrón de ejecución reutilizable:

```bash
cat sql/top_ventas_marcas_lps.sql | ssh -o BatchMode=yes ronal@201.190.38.238 \
  'P=$(grep PRUEBAS_MYSQL_ROOT_PASSWORD /home/ronal/pruebas-dulce/stack/.env | cut -d= -f2); \
   docker exec -i mysql-pruebas-dulce mysql -uroot -p"$P" -B --default-character-set=utf8mb4'
```

> Encoding: exportar siempre con `--default-character-set=utf8mb4` (los nombres llevan tildes/ñ).
> Con `--table` el output sale mal y hubo que forzar `latin1`; con `-B` (TSV) utf8mb4 funciona bien.

## Decisiones de modelado (importantes para el feature)

1. **`tipo_articulo` NO sirve como categoría.** Solo tiene dos valores: Bienes (186 artículos) y
   Servicios (8). El primer reporte (`sql/top_ventas.sql`) se descartó por esto.
2. **La categoría real del negocio vive en la tabla `marcas`**: Panadería, Pastelería, Repostería,
   Cafetería y TECNO. El cliente usó `marcas` como si fuera categoría.
3. **`TECNO` se excluye siempre**: son servicios técnicos internos facturados a L 1.00
   (L 198,220 el último año) — ruido puro que distorsiona cualquier ranking.
4. Filtro base de todas las consultas: `UNION ALL` de cajas 1+3+4, `estado_factura <> 'NULA'`,
   join `detalle_factura` → `articulo` → `marcas`.

## Los reportes

### 1. Top 10 por unidades, por categoría — `sql/top_ventas_marcas.sql`
Ranking dentro de cada categoría por unidades vendidas (último año móvil).

### 2. Top 10 por facturación, por categoría — `sql/top_ventas_marcas_lps.sql`
Mismo ranking pero por L, con `pct_categoria` (% de participación dentro de la categoría).
Es el más revelador: separa "lo que más se vende" de "lo que más deja".

### 3. Comparativo trimestral — `sql/trimestres_2024.sql`
`YEAR` × `QUARTER` × categoría, en unidades y L, desde 2024.
(`sql/trimestres.sql` es la versión previa, solo 2025+.)

### 4. Ventana de julio comparable — `sql/julio_ventana.sql`
1–18 de julio 2025 vs 2026 por categoría. Auxiliar: es la base del factor de proyección.

## Hallazgos (snapshot 2026-07-18)

**Facturación último año por categoría:**
Pastelería L 1,125,858 · Panadería L 855,470 · Repostería L 506,416 · Cafetería L 86,193.

**Volumen vs valor** — el hallazgo central:
- Panadería mueve el volumen: Semita concha 8,646 uds (L 138,188), Dona sencilla 6,061 uds.
- Pastelería mueve el dinero: **Pastel mediano, L 355,364 con solo 372 unidades** (31.6% de su categoría).
- Pastel 3 leches porción es 8º en unidades pero 3º en dinero.

**Crecimiento:**
- 2024 L 1,358,215 → 2025 L 2,129,566 (**+56.8%**) → 2026 proyectado L 3,051,670 (**+43.3%**)
- T1-2026 +50.0% y T2-2026 +47.6% contra los mismos trimestres de 2025
- Repostería es la que más acelera (+73–83%)
- Cafetería es línea nueva: arranca en T3-2025

**Alerta:** Panadería viene **-12% en julio-2026 vs julio-2025** pese a un H1 fuerte.
Su proyección anual probablemente es optimista.

### Metodología de la proyección
No se extrapoló linealmente. Se calculó el factor de crecimiento real del período
1-ene → 18-jul 2026 contra el mismo período de 2025, y ese factor se aplicó a los
**T3/T4 reales de 2025** — así se respeta la estacionalidad del negocio en lugar de asumir
trimestres planos. En el Excel las celdas proyectadas van en amarillo y la metodología queda
anotada en la hoja.

## Notas para el feature del POS

- Las cuatro consultas son directamente portables a endpoints de la API: el patrón
  `UNION ALL de cajas → join a catálogo común → agregación` ya es el usado por
  `/invoices/admin` (ver memoria `project_us099_facturas_total`), así que hay de dónde reusar.
- La agrupación por `marcas` es específica de cómo este cliente cargó su catálogo. Si el reporte
  se generaliza, la dimensión de agrupación debería ser configurable (marca / tipo / categoría),
  no hardcodeada.
- La exclusión de `TECNO` es un caso particular de "artículos que no son venta real" — vale la
  pena pensarlo como una lista de exclusión configurable.
- El comparativo trimestral y la proyección son buenos candidatos a dashboard; los tops, a
  reporte con filtro de rango de fechas.
