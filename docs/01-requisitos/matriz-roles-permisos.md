# Matriz de roles y permisos

El control de acceso es **RBAC con permisos atómicos**: los roles son colecciones de
permisos y los endpoints exigen permisos, nunca roles. Así, crear un rol nuevo desde el
panel de administración no requiere tocar código.

Formato del permiso: `recurso:acción`.

## Catálogo de permisos

| Permiso | Descripción |
|---|---|
| `case:create:self` | Radicar una solicitud propia |
| `case:create:staff` | Radicar un reporte sobre un estudiante |
| `case:read:own` | Ver los casos que radicó o que le son propios |
| `case:read:assigned` | Ver los casos asignados a sí mismo o a su área |
| `case:read:any` | Ver cualquier caso, incluidos los confidenciales |
| `case:update:classification` | Confirmar o corregir la clasificación |
| `case:assign` | Asignar y reasignar casos |
| `case:transition` | Cambiar el estado de un caso |
| `case:followup:create` | Registrar seguimientos |
| `case:close` | Cerrar casos |
| `student:read:dossier` | Ver el expediente consolidado de un estudiante |
| `student:read:group` | Ver los estudiantes de los grupos propios |
| `student:import` | Cargar estudiantes desde archivo |
| `mood:create:self` | Registrar el check-in de ánimo propio |
| `mood:read:aggregate` | Ver tendencias de ánimo agregadas |
| `risk:read:student` | Ver el puntaje y los factores de riesgo de un estudiante |
| `risk:read:dashboard` | Ver el tablero agregado de riesgo |
| `risk:model:manage` | Editar factores, pesos y umbrales del modelo de riesgo |
| `report:generate` | Generar reportes institucionales |
| `report:template:manage` | Crear y versionar plantillas de reporte |
| `user:read` · `user:manage` | Consultar / crear, editar y desactivar usuarios |
| `role:read` · `role:manage` | Consultar / crear y editar roles y sus permisos |
| `audit:read` | Consultar la bitácora de auditoría |
| `ai:metrics:read` | Ver métricas de precisión del clasificador |

## Roles predefinidos

| Permiso | Estudiante | Docente | Profesional bienestar | Coordinador permanencia | Administrador |
|---|:--:|:--:|:--:|:--:|:--:|
| `case:create:self` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `case:create:staff` | — | ✅ | ✅ | ✅ | ✅ |
| `case:read:own` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `case:read:assigned` | — | — | ✅ | ✅ | ✅ |
| `case:read:any` | — | — | — | ✅ | ✅ |
| `case:update:classification` | — | — | ✅ | ✅ | ✅ |
| `case:assign` | — | — | — | ✅ | ✅ |
| `case:transition` | — | — | ✅ | ✅ | ✅ |
| `case:followup:create` | — | — | ✅ | ✅ | ✅ |
| `case:close` | — | — | ✅ | ✅ | ✅ |
| `student:read:dossier` | — | — | ✅ | ✅ | ✅ |
| `student:read:group` | — | ✅ | ✅ | ✅ | ✅ |
| `student:import` | — | — | — | — | ✅ |
| `mood:create:self` | ✅ | — | — | — | — |
| `mood:read:aggregate` | — | — | ✅ | ✅ | ✅ |
| `risk:read:student` | — | — | ✅ | ✅ | ✅ |
| `risk:read:dashboard` | — | — | ✅ | ✅ | ✅ |
| `risk:model:manage` | — | — | — | — | ✅ |
| `report:generate` | — | — | — | ✅ | ✅ |
| `report:template:manage` | — | — | — | — | ✅ |
| `user:read` / `user:manage` | — | — | — | — | ✅ |
| `role:read` / `role:manage` | — | — | — | — | ✅ |
| `audit:read` | — | — | — | — | ✅ |
| `ai:metrics:read` | — | — | — | ✅ | ✅ |

## Reglas de acceso a nivel de fila

Los permisos no bastan: además se aplican filtros de alcance en la consulta.

| Regla | Enunciado |
|---|---|
| R1 | El **docente** solo ve estudiantes matriculados en los grupos donde figura como titular en el período activo. |
| R2 | El **docente** ve únicamente los casos que él radicó; nunca los de otros docentes ni los autorreportes del estudiante. |
| R3 | El **estudiante** ve solo sus propios casos y solo los seguimientos marcados como visibles para él. |
| R4 | Un caso marcado **confidencial** se restringe al área responsable, al radicador y a quien tenga `case:read:any`. |
| R5 | El **puntaje de riesgo nunca es visible para el docente ni para el estudiante**, para evitar estigmatización y efectos de profecía autocumplida. |
| R6 | Los casos de categoría psicológica exigen, además del permiso, pertenencia al área de psicología. |
| R7 | Todo acceso que supere el alcance propio (`case:read:any`, `student:read:dossier`) queda auditado. |

## Decisiones abiertas

- ¿El coordinador de programa debe ver el riesgo individual o solo el agregado de su
  programa? Propuesta por defecto: **solo agregado**, con individual bajo solicitud
  registrada. Confirmar con el área de bienestar antes de la fase 5.
