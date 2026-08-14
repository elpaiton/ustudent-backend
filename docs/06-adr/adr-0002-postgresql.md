# ADR-0002 · PostgreSQL como almacén único

- **Estado**: aceptado
- **Fecha**: 2026-08-13

## Contexto

El sistema maneja datos claramente relacionales (usuarios, roles, matrículas, casos) y
también estructuras variables: el desglose de factores de riesgo, los parámetros del modelo,
la respuesta cruda del clasificador y las secciones de las plantillas de reporte. Además,
necesita búsqueda por texto en los casos y agregaciones para el tablero.

## Decisión

**PostgreSQL 16 como único almacén**, aprovechando:

- Tablas relacionales con restricciones reales (FK, `CHECK`, índices únicos parciales).
- **JSONB** para lo genuinamente variable: `factor_breakdown`, `thresholds`, `params`,
  `raw_response`, `sections`, `metadata` de auditoría.
- **Búsqueda de texto** nativa con `to_tsvector('spanish', ...)` e índice GIN.
- `citext` para correos, `inet` para direcciones IP en auditoría, arreglos para las
  etiquetas de los check-ins.

El esquema evoluciona **solo** por migraciones Flyway versionadas.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| PostgreSQL + MongoDB para lo variable | Dos almacenes que sincronizar y respaldar, sin ganancia real: JSONB cubre el caso. |
| PostgreSQL + Elasticsearch para búsqueda | El volumen (decenas de miles de casos) no lo justifica; la búsqueda de texto nativa basta. |
| MySQL | Soporte de JSON y de tipos (`citext`, arreglos, índices parciales) inferior para este uso. |
| Redis para caché | Se pospone. La caché en memoria de la aplicación cubre permisos y catálogos. |

## Consecuencias

**Positivas**
- Un solo respaldo, una sola restauración, una sola conexión que asegurar.
- Transacciones ACID sobre todo el modelo, incluido el histórico de riesgo.
- Testcontainers levanta el mismo motor que producción en las pruebas.

**Negativas**
- Las consultas sobre JSONB son menos legibles y requieren índices GIN específicos.
- Una carga analítica pesada competiría con la transaccional en la misma instancia.

**Mitigaciones**
- Lo que se consulta y filtra vive en columnas propias; JSONB solo guarda el detalle que se
  lee completo (regla: *si se filtra, es columna*).
- Si el tablero pesa, se materializan vistas por período antes que introducir otro motor.
