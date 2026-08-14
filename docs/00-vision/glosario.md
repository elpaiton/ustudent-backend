# Glosario (lenguaje ubicuo)

Términos del dominio. El nombre en inglés es el que se usa en código, tablas y endpoints.

| Español | Inglés (código) | Definición |
|---|---|---|
| Caso | `Case` | Unidad de trabajo del sistema. Agrupa una solicitud o reporte, su clasificación, sus seguimientos y su resolución. |
| Solicitud | `SelfRequest` | Caso cuyo origen es el propio estudiante. |
| Reporte | `StaffReport` | Caso cuyo origen es un docente o funcionario y que referencia a un estudiante. |
| Origen del caso | `CaseOrigin` | `SELF` \| `STAFF`. Determina quién lo radicó, no de quién trata. |
| Sujeto del caso | `subjectStudent` | Estudiante al que se refiere el caso. Siempre existe, sea quien sea el radicador. |
| Radicador | `reporter` | Usuario que creó el caso. |
| Categoría | `CaseCategory` | Clasificación temática: académica, inasistencia, psicológica, económica, personal/familiar, salud física, convivencia, administrativa. |
| Prioridad | `CasePriority` | `LOW` \| `MEDIUM` \| `HIGH` \| `CRITICAL`. Deriva de la categoría y de señales del texto. |
| Área responsable | `HandlingUnit` | Dependencia que atiende: bienestar, psicología, consejería académica, apoyo financiero, secretaría. |
| Seguimiento | `CaseFollowUp` | Nota fechada asociada a un caso, escrita por quien lo atiende. |
| Expediente | `StudentDossier` | Vista consolidada de todos los casos, check-ins y alertas de un estudiante. |
| Check-in de ánimo | `MoodCheckIn` | Registro breve y periódico del estado del estudiante (escala 1–5 + etiquetas opcionales). |
| Factor de riesgo | `RiskFactor` | Variable observable que aporta al puntaje: inasistencia, casos críticos abiertos, tendencia de ánimo, etc. |
| Puntaje de riesgo | `RiskScore` | Valor 0–100 que estima la propensión a la deserción del estudiante en el período. |
| Nivel de alerta | `RiskLevel` | `LOW` \| `MODERATE` \| `HIGH` \| `CRITICAL`, derivado del puntaje por umbrales. |
| Evaluación de riesgo | `RiskAssessment` | Cálculo puntual del puntaje, con la contribución de cada factor y la versión del modelo de reglas usada. |
| Modelo de riesgo | `RiskModel` | Conjunto versionado de factores, pesos y umbrales vigentes. |
| Clasificación | `Classification` | Resultado del clasificador: categoría, prioridad, área, confianza y proveedor que la produjo. |
| Reporte general | `InstitutionalReport` | Informe agregado por período/programa generado desde una plantilla. |
| Plantilla | `ReportTemplate` | Definición versionada de las secciones y variables del reporte general. |
| Permiso | `Permission` | Capacidad atómica, con formato `recurso:acción` (p. ej. `case:assign`). |
| Rol | `Role` | Conjunto nombrado de permisos asignable a usuarios. |
