# Observabilidad

## Registros (logs)

- Formato **JSON** en `dev`, `staging` y `prod`; legible en `local`.
- Campos obligatorios: `timestamp`, `level`, `logger`, `traceId`, `userId` (público),
  `message`.
- Niveles: `ERROR` requiere acción humana; `WARN` es anomalía tolerada; `INFO` marca hitos de
  negocio; `DEBUG` solo fuera de producción.

### Qué nunca se registra

Contraseñas, tokens, cabecera `Authorization`, descripción de casos, notas de seguimiento,
comentarios de check-in, documentos de identidad y correos completos. Un filtro de logback
enmascara estos patrones como última red de seguridad, pero la primera responsabilidad es no
escribirlos.

## Trazas

`traceId` generado en el filtro de entrada y propagado por MDC a toda la petición, incluidos
los trabajos asíncronos (se copia el contexto al encolar). El `traceId` se devuelve en el
cuerpo del error RFC 7807, de modo que un usuario puede reportar un problema y el equipo
encontrar exactamente su petición.

## Métricas (Micrometer → `/actuator/prometheus`)

### Técnicas
`http_server_requests` (latencia p50/p95/p99 por endpoint), pool de conexiones HikariCP,
memoria y GC de la JVM, trabajos pendientes en `sys_async_jobs`.

### De negocio

| Métrica | Tipo | Para qué |
|---|---|---|
| `ustudent_cases_created_total{origin,category}` | contador | Volumen y mezcla de casos |
| `ustudent_case_first_response_seconds{priority}` | histograma | Cumplimiento de tiempos objetivo |
| `ustudent_classification_duration_seconds{provider}` | histograma | Latencia del clasificador |
| `ustudent_classification_fallback_total` | contador | Salud de la integración con IA |
| `ustudent_classification_corrected_total{category}` | contador | Precisión percibida |
| `ustudent_urgency_signals_total{signal}` | contador | Volumen de casos críticos detectados |
| `ustudent_risk_assessments_total{trigger,level}` | contador | Actividad del motor |
| `ustudent_risk_level_students{level}` | medidor | Distribución actual de la población |
| `ustudent_auth_failures_total{reason}` | contador | Indicio de fuerza bruta |

## Alertas

| Alerta | Condición | Severidad |
|---|---|---|
| API caída | `/actuator/health` falla 3 veces seguidas | Crítica |
| Latencia degradada | p95 > 1 s durante 5 min | Alta |
| Respaldo de IA excesivo | `fallback_total` > 20 % de las clasificaciones en 1 h | Media |
| Trabajos atascados | Más de 50 trabajos `PENDING` con `run_after` vencido | Alta |
| Job de riesgo no ejecutado | Sin ejecución `SCHEDULED` en 26 h | Alta |
| Fallos de autenticación | > 100 fallos en 10 min | Alta (posible ataque) |
| Espacio en disco | > 85 % en el volumen de la BD | Alta |

## Salud

| Sonda | Endpoint | Comprueba |
|---|---|---|
| Vivacidad | `/actuator/health/liveness` | El proceso responde |
| Disponibilidad | `/actuator/health/readiness` | Base de datos y migraciones al día |
| Detalle | `/actuator/health` (restringido) | Componentes: BD, SMTP, proveedor de IA, almacenamiento |

El proveedor de IA se reporta como componente **degradable**: que esté caído no marca la
aplicación como no disponible, porque el sistema sigue clasificando con el respaldo.

## Auditoría frente a logs

No son lo mismo y no se mezclan:

| | Logs | Auditoría |
|---|---|---|
| Propósito | Diagnóstico técnico | Rendición de cuentas |
| Destino | Fichero / agregador | Tabla `sys_audit_log` |
| Retención | 30 días | 5 años |
| Contenido | Técnico, sin datos sensibles | Quién, qué, cuándo, sobre qué recurso |
| Consultable por | Equipo técnico | Administrador, desde la aplicación |
