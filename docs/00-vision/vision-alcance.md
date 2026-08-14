# Visión y alcance

## Problema

Las áreas de bienestar y permanencia universitaria detectan tarde las señales de riesgo de
deserción. La información está dispersa: el docente ve la inasistencia pero no el contexto
personal, el psicólogo ve la consulta pero no el rendimiento, y el estudiante no siempre
sabe a quién acudir ni qué pasó con su solicitud. Cuando se consolida un informe, se hace a
mano sobre hojas de cálculo y llega con semanas de retraso.

## Propuesta

Un único punto de entrada para **solicitudes y reportes de bienestar estudiantil**, con
tres propiedades que lo diferencian de un simple gestor de tickets:

1. **Doble origen.** El caso puede radicarlo el estudiante (autorreporte) o un docente
   referido a un estudiante. Ambos caen en el mismo expediente del estudiante.
2. **Clasificación asistida por IA.** El texto libre se clasifica automáticamente en
   categoría, prioridad y área responsable, con revisión humana antes de cerrar.
3. **Riesgo de deserción calculado.** Un motor de reglas configurable convierte el historial
   de casos, la inasistencia y las señales de ánimo en un porcentaje de riesgo por
   estudiante, explicable factor por factor.

## Objetivos

| # | Objetivo | Métrica de éxito |
|---|---|---|
| O1 | Centralizar el 100 % de solicitudes de bienestar | Casos radicados en la plataforma / casos totales del período |
| O2 | Reducir el tiempo de primera respuesta | Mediana de horas entre radicación y primera atención < 24 h |
| O3 | Detectar riesgo antes del retiro | % de estudiantes con alerta emitida al menos 30 días antes del retiro formal |
| O4 | Eliminar la consolidación manual de informes | Informe institucional generado en < 1 min desde la plataforma |
| O5 | Clasificación automática confiable | ≥ 80 % de casos con categoría sugerida aceptada sin corrección |

## Alcance (versión 1)

**Incluido**
- Gestión de identidad: usuarios, roles, permisos, asignación por administrador.
- Radicación de casos por estudiante y por docente, con adjuntos.
- Ciclo de vida del caso: radicado → clasificado → asignado → en atención → resuelto → cerrado.
- Clasificación automática de categoría, prioridad y área, con confirmación humana.
- Expediente por estudiante: línea de tiempo de todos sus casos y seguimientos.
- Registro periódico de estado de ánimo del estudiante (check-in breve).
- Motor de riesgo de deserción con factores y pesos parametrizables desde el panel de admin.
- Tablero de riesgo por programa, semestre y nivel de alerta.
- Generación de reporte general institucional desde plantilla, exportable a PDF.
- Auditoría de acciones sensibles.

**Fuera de alcance (versión 1)**
- Integración en línea con el sistema académico institucional (se cubre con carga CSV).
- App móvil nativa (la web es responsive).
- Chat en tiempo real entre estudiante y profesional.
- Historia clínica psicológica formal — se registran notas de seguimiento, no diagnóstico clínico.
- Modelo de machine learning entrenado con datos históricos propios (ver [ADR-0004](../06-adr/adr-0004-riesgo-por-reglas.md)).

## Restricciones

- **Privacidad.** Los casos contienen datos sensibles (salud mental, situación familiar,
  situación económica). El acceso es estrictamente por necesidad de conocer; ver
  [modelo de seguridad](../02-arquitectura/seguridad.md).
- **Backend monolítico.** Decisión del equipo, registrada en [ADR-0001](../06-adr/adr-0001-monolito-modular.md).
- **Equipo pequeño.** El diseño evita dependencias operativas que no se puedan sostener.

## Supuestos

- El listado de estudiantes, programas y matrículas se carga por archivo al inicio de cada período.
- La institución dispone de un servidor SMTP para notificaciones.
- Existe presupuesto para un proveedor de LLM por API, o en su defecto se opera con el
  clasificador determinista de respaldo.

## Riesgos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Baja adopción docente | Alto | Formulario de reporte de ≤ 4 campos; reporte en menos de 1 minuto |
| Falsos positivos de riesgo estigmatizan al estudiante | Alto | El riesgo nunca es visible para el docente; solo bienestar y admin lo ven |
| Dependencia del proveedor de IA | Medio | Puerto abstracto + clasificador de respaldo por reglas |
| Datos sensibles filtrados | Crítico | Cifrado en reposo del cuerpo del caso, auditoría, RBAC estricto |
