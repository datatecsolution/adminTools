---
name: reference-api-test-mockito-jdbc
description: Patrones Mockito reusables para tests unitarios de la API Spring Boot (Desktop/admintools) — servicios que usan JdbcTemplate/varargs
metadata:
  type: reference
---

API Java = `/Users/jdmayorga/Desktop/admintools`. Stack de test: JUnit 5 + Mockito + AssertJ (dentro de `spring-boot-starter-test`). Convención: unit puros con `@ExtendWith(MockitoExtension.class)` en `src/test/.../domain/service/*Test.java`; slices de controller con `@WebMvcTest(controllers=X.class)` + `@AutoConfigureMockMvc(addFilters=false)` + `@MockBean` en `.../web/controller/*CtlTest.java`. Nombres de test en español descriptivos. AssertJ `isEqualByComparingTo` para BigDecimal.

Dos gotchas al mockear servicios que hablan por `JdbcTemplate` (patrón cross-DB de reportes/importers):

1. **Varargs de `queryForMap(String, Object...)`**: `when(jdbc.queryForMap(anyString(), any(), any()))` NO matchea (los matchers degeneran a literales `"", null, null` → `PotentialStubbingProblem`). Usar `when(jdbc.queryForMap(anyString(), any(Object[].class)))` — matchea el array de varargs completo.

2. **`NamedParameterJdbcTemplate` construido dentro del service** (`new NamedParameterJdbcTemplate(jdbc)` con jdbc mock): sus `queryForList(sql, Map, Class)` terminan llamando `jdbc.query(PreparedStatementCreator, RowMapper)` sobre el mock (la impl interna no corre). Stub: `when(jdbc.query(any(PreparedStatementCreator.class), any(RowMapper.class))).thenReturn(List.of())`. Los `jdbc.query(String, RowCallbackHandler)` (p.ej. catálogo de impuestos) se stubean con `doAnswer` invocando `h.processRow(rsMock)`.

3. Service que crea su propio `JdbcTemplate` en el constructor desde un `@Qualifier("commonDataSource") DataSource` (ej. `DailyReportService`): pasar un DataSource mock al constructor y suplantar el campo con `ReflectionTestUtils.setField(svc, "jdbc", jdbcMock)`. Secreto `@Value` (ej. `InvoiceQrTokenService.secret`): `ReflectionTestUtils.setField(svc, "secret", ...)`.

Para tests de importers (`domain/service/importer`) conviene mockear `TabularFileParser` y devolver `ParsedFile`/`ParsedRow` en memoria; usar `@MockitoSettings(strictness = LENIENT)` porque el setup comparte stubs (impuestos, dedup DB) que no todos los tests ejercen. `TabularFileParser` real se testea con `MockMultipartFile` (CSV) y `XSSFWorkbook` en memoria (xlsx).
