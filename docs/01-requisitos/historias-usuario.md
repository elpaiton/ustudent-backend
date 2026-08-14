# Historias de usuario

Formato: `US-<épica>-<n>`. La columna **Fase** remite al [roadmap](../08-plan/roadmap.md).

## E1 · Identidad y acceso

| ID | Historia | Criterios de aceptación | Fase |
|---|---|---|---|
| US-E1-1 | Como usuario quiero iniciar sesión con mi correo institucional para acceder a la plataforma | Credenciales válidas devuelven token y redirigen al panel de mi rol; 5 intentos fallidos bloquean 15 min; la sesión expira a los 30 min de inactividad | 1 |
| US-E1-2 | Como administrador quiero crear, editar y desactivar usuarios | Puedo crear con correo, documento, nombre y rol; desactivar no borra datos y revoca sesiones activas; el correo es único | 1 |
| US-E1-3 | Como administrador quiero definir roles y asignarles permisos | Puedo crear un rol y marcar permisos de la lista; los cambios aplican en la siguiente petición del usuario afectado; no puedo eliminar un rol con usuarios asignados | 1 |
| US-E1-4 | Como administrador quiero cargar estudiantes y docentes desde un archivo | Subo un CSV con el formato publicado; el sistema reporta filas válidas, filas con error y su motivo; nada se inserta si hay error de formato de encabezado | 2 |
| US-E1-5 | Como usuario quiero recuperar mi contraseña | Recibo un correo con enlace de un solo uso válido 30 min; el enlace usado o vencido muestra mensaje claro | 2 |

## E2 · Radicación de casos

| ID | Historia | Criterios de aceptación | Fase |
|---|---|---|---|
| US-E2-1 | Como estudiante quiero radicar una solicitud describiendo mi situación | Formulario con categoría sugerida opcional, descripción libre (≥ 20 caracteres) y adjuntos hasta 10 MB; al guardar recibo número de radicado y confirmación por correo | 2 |
| US-E2-2 | Como estudiante quiero marcar mi solicitud como confidencial | Al marcarla, solo el área responsable y el admin la ven; nunca aparece para docentes | 2 |
| US-E2-3 | Como docente quiero reportar la situación de un estudiante de mis grupos | Busco al estudiante por documento o nombre entre los de mis grupos; describo el hecho; el caso queda ligado al expediente del estudiante | 2 |
| US-E2-4 | Como docente quiero reportar inasistencia recurrente en pocos clics | Formulario corto: estudiante, número de sesiones faltadas, período, observación opcional; se envía en menos de 1 minuto | 2 |
| US-E2-5 | Como estudiante quiero ver el estado de mis solicitudes | Listado con radicado, fecha, categoría, estado y última actualización; detalle con línea de tiempo | 2 |
| US-E2-6 | Como estudiante quiero recibir notificación cuando mi caso cambia de estado | Correo y notificación en la app en cada transición de estado y en cada respuesta del área | 3 |

## E3 · Atención de casos

| ID | Historia | Criterios de aceptación | Fase |
|---|---|---|---|
| US-E3-1 | Como profesional de bienestar quiero ver mi bandeja de casos asignados | Filtros por estado, prioridad, categoría y antigüedad; orden por prioridad y fecha; paginado | 3 |
| US-E3-2 | Como profesional quiero registrar seguimientos en un caso | Nota fechada con autor; queda en la línea de tiempo; no se puede editar tras 24 h, solo agregar corrección | 3 |
| US-E3-3 | Como profesional quiero cambiar el estado de un caso | Solo transiciones válidas según la [máquina de estados](../04-diagramas/estados/ciclo-vida-caso.md); cerrar exige motivo de cierre | 3 |
| US-E3-4 | Como coordinador quiero reasignar casos entre profesionales | Selecciono uno o varios casos y un destinatario; queda registrado en auditoría con motivo | 3 |
| US-E3-5 | Como profesional quiero ver el expediente completo del estudiante | Línea de tiempo de casos, seguimientos, check-ins y alertas; los casos confidenciales de otras áreas aparecen ocultos salvo permiso explícito | 3 |

## E4 · Clasificación con IA

| ID | Historia | Criterios de aceptación | Fase |
|---|---|---|---|
| US-E4-1 | Como sistema quiero clasificar cada caso al radicarse | Se sugiere categoría, prioridad y área con un valor de confianza; la clasificación no bloquea la radicación (es asíncrona, ≤ 30 s) | 4 |
| US-E4-2 | Como profesional quiero aceptar o corregir la clasificación sugerida | Veo la sugerencia y la confianza; al corregir se guarda la categoría final y la original para medir precisión | 4 |
| US-E4-3 | Como sistema quiero escalar automáticamente señales críticas | Si el clasificador detecta riesgo de autolesión o violencia, el caso se marca `CRITICAL` y se notifica de inmediato al área de psicología, sin esperar revisión | 4 |
| US-E4-4 | Como administrador quiero ver la precisión del clasificador | Tablero con % de sugerencias aceptadas sin corrección, por categoría y por mes | 5 |
| US-E4-5 | Como administrador quiero operar si el proveedor de IA falla | Ante error o timeout, se aplica el clasificador de reglas y el caso queda marcado `PENDING_REVIEW`; el sistema nunca queda bloqueado | 4 |

## E5 · Riesgo de deserción

| ID | Historia | Criterios de aceptación | Fase |
|---|---|---|---|
| US-E5-1 | Como estudiante quiero registrar cómo me siento periódicamente | Check-in de 3 toques: escala 1–5, etiquetas opcionales, comentario opcional; máximo uno por día | 4 |
| US-E5-2 | Como sistema quiero calcular el puntaje de riesgo de cada estudiante | Se recalcula al cerrar/abrir un caso, al registrar un check-in y en un job diario; se persiste con el detalle de cada factor | 5 |
| US-E5-3 | Como profesional quiero entender por qué un estudiante está en riesgo | El detalle muestra cada factor, su valor, su peso y los puntos que aportó; sin explicación no se muestra el puntaje | 5 |
| US-E5-4 | Como administrador quiero ajustar pesos y umbrales sin tocar código | Panel de parámetros; guardar crea una nueva versión del modelo; las evaluaciones históricas conservan la versión con la que se calcularon | 5 |
| US-E5-5 | Como coordinador quiero un tablero de riesgo por programa | Distribución por nivel de alerta, tendencia mensual, top de factores más frecuentes, filtros por programa y semestre | 5 |
| US-E5-6 | Como coordinador quiero recibir alerta cuando un estudiante sube de nivel | Notificación al cruzar a `HIGH` o `CRITICAL`, con enlace al expediente | 5 |

## E6 · Reportes institucionales

| ID | Historia | Criterios de aceptación | Fase |
|---|---|---|---|
| US-E6-1 | Como administrador quiero generar el reporte general del período | Elijo plantilla, período y filtros; el sistema produce el informe con las secciones definidas y lo descargo en PDF | 6 |
| US-E6-2 | Como administrador quiero gestionar plantillas de reporte | Puedo crear y versionar plantillas indicando secciones y variables disponibles | 6 |
| US-E6-3 | Como administrador quiero exportar datos agregados | Descarga CSV de casos y de riesgo, siempre agregada o anonimizada según el permiso | 6 |

## E7 · Transversales

| ID | Historia | Criterios de aceptación | Fase |
|---|---|---|---|
| US-E7-1 | Como administrador quiero auditar quién accedió a información sensible | Registro inmutable con usuario, acción, recurso, fecha e IP; consultable y filtrable | 3 |
| US-E7-2 | Como usuario quiero una interfaz clara en móvil y escritorio | Todas las pantallas usables desde 360 px; contraste AA; navegación por teclado | 2 |
| US-E7-3 | Como usuario quiero que la aplicación esté en español | Textos, fechas y formatos en `es-CO`; la arquitectura no impide agregar otro idioma después | 2 |
