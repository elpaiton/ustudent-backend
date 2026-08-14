# Contrato de API REST

Base: `/api/v1`. Autenticación por JWT en cookie `httpOnly` (o `Authorization: Bearer` para
clientes no navegador). Errores en formato RFC 7807.

## Convenciones

- Paginación: `?page=0&size=20&sort=submittedAt,desc`.
- Los identificadores expuestos son UUID (`public_id`), nunca la clave primaria interna.
- Fechas ISO-8601 en UTC.
- Cabecera `Idempotency-Key` obligatoria en `POST /cases`.

### Formato de error

```json
{
  "type": "https://ustudent.usta.edu.co/errors/case-invalid-transition",
  "title": "Transición de estado no permitida",
  "status": 409,
  "detail": "No se puede pasar de CLOSED a IN_PROGRESS",
  "instance": "/api/v1/cases/018f.../transitions",
  "code": "CASE_INVALID_TRANSITION",
  "traceId": "b7d1c2e4a9",
  "errors": []
}
```

## Autenticación · `/auth`

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| POST | `/auth/login` | público | Emite tokens de acceso y refresco |
| POST | `/auth/refresh` | público (cookie) | Rota el token de refresco |
| POST | `/auth/logout` | autenticado | Revoca la sesión actual |
| GET | `/auth/me` | autenticado | Perfil, roles y permisos efectivos |
| POST | `/auth/password/forgot` | público | Envía enlace de restablecimiento |
| POST | `/auth/password/reset` | público (token) | Restablece la contraseña |

## Usuarios, roles y permisos · `/admin`

| Método | Ruta | Permiso |
|---|---|---|
| GET · POST | `/admin/users` | `user:read` · `user:manage` |
| GET · PATCH | `/admin/users/{id}` | `user:read` · `user:manage` |
| POST | `/admin/users/{id}/status` | `user:manage` |
| PUT | `/admin/users/{id}/roles` | `user:manage` |
| GET · POST | `/admin/roles` | `role:read` · `role:manage` |
| GET · PATCH · DELETE | `/admin/roles/{id}` | `role:read` · `role:manage` |
| PUT | `/admin/roles/{id}/permissions` | `role:manage` |
| GET | `/admin/permissions` | `role:read` |
| GET | `/admin/audit` | `audit:read` |

## Académico · `/students`, `/teachers`

| Método | Ruta | Permiso | Notas |
|---|---|---|---|
| GET | `/students` | `student:read:group` | Filtrado por alcance del actor |
| GET | `/students/{id}` | `student:read:dossier` | Datos básicos |
| GET | `/students/{id}/dossier` | `student:read:dossier` | Casos, check-ins y alertas; auditado |
| GET | `/teachers/me/groups` | autenticado | Grupos del período activo |
| POST | `/admin/imports/students` | `student:import` | Carga CSV, respuesta con resumen de filas |

## Casos · `/cases`

| Método | Ruta | Permiso | Notas |
|---|---|---|---|
| POST | `/cases` | `case:create:self` \| `case:create:staff` | Cuerpo distinto según `origin` |
| GET | `/cases` | según alcance | Filtros: `status`, `category`, `priority`, `assignee`, `studentId`, `from`, `to` |
| GET | `/cases/{id}` | según alcance | 404 (no 403) si está fuera de alcance, para no filtrar existencia |
| PATCH | `/cases/{id}` | `case:update:classification` | Corrige categoría, prioridad y área |
| POST | `/cases/{id}/transitions` | `case:transition` | `{ "to": "IN_PROGRESS", "reason": "..." }` |
| POST | `/cases/{id}/assign` | `case:assign` | `{ "assigneeId": "...", "reason": "..." }` |
| GET · POST | `/cases/{id}/follow-ups` | `case:followup:create` | `visibleToStudent` booleano |
| POST | `/cases/{id}/attachments` | radicador o área | `multipart/form-data`, ≤ 10 MB |
| GET | `/cases/{id}/classification` | `case:read:*` | Sugerencia, confianza y proveedor |
| GET | `/cases/me` | `case:read:own` | Casos del usuario autenticado |

### `POST /cases` — solicitud del estudiante

```json
{
  "origin": "SELF",
  "title": "Dificultad para continuar el semestre",
  "description": "Llevo tres semanas sin poder concentrarme...",
  "suggestedCategory": "PSYCHOLOGICAL",
  "isConfidential": true
}
```

### `POST /cases` — reporte del docente

```json
{
  "origin": "STAFF",
  "subjectStudentId": "018f2c...",
  "courseGroupId": "018f31...",
  "title": "Inasistencia recurrente",
  "description": "No asiste desde la semana 4; no responde correos.",
  "suggestedCategory": "ATTENDANCE",
  "attendance": { "missedSessions": 6, "period": "2026-2" }
}
```

### Respuesta (201)

```json
{
  "id": "018f4a...",
  "caseNumber": "US-2026-000123",
  "status": "SUBMITTED",
  "classificationStatus": "PENDING",
  "submittedAt": "2026-08-13T14:22:05Z"
}
```

`classificationStatus` es `PENDING` porque la clasificación es asíncrona: el cliente hace
*polling* de `/cases/{id}` o espera la notificación.

## Ánimo · `/mood`

| Método | Ruta | Permiso |
|---|---|---|
| POST | `/mood/check-ins` | `mood:create:self` |
| GET | `/mood/check-ins/me` | `mood:create:self` |
| GET | `/mood/aggregate` | `mood:read:aggregate` |

```json
{ "score": 2, "tags": ["ansiedad", "sobrecarga"], "comment": "opcional" }
```

## Riesgo · `/risk`

| Método | Ruta | Permiso | Notas |
|---|---|---|---|
| GET | `/risk/students/{id}` | `risk:read:student` | Puntaje, nivel y **desglose obligatorio** por factor |
| POST | `/risk/students/{id}/recalculate` | `risk:read:student` | Disparador `MANUAL` |
| GET | `/risk/dashboard` | `risk:read:dashboard` | Agregados por programa, semestre y nivel |
| GET | `/risk/students/{id}/history` | `risk:read:student` | Serie temporal de evaluaciones |
| GET · POST | `/risk/models` | `risk:model:manage` | Listar / crear versión |
| POST | `/risk/models/{id}/activate` | `risk:model:manage` | Activa una versión |
| PUT | `/risk/models/{id}/factors` | `risk:model:manage` | Pesos y parámetros |

### `GET /risk/students/{id}`

```json
{
  "studentId": "018f2c...",
  "score": 42.5,
  "level": "MODERATE",
  "modelVersion": "1.0.0",
  "computedAt": "2026-08-13T07:00:00Z",
  "trigger": "SCHEDULED",
  "factors": [
    { "code": "ATTENDANCE", "label": "Inasistencia", "value": 0.75, "weight": 20, "points": 15.0 },
    { "code": "MOOD_TREND", "label": "Tendencia de ánimo", "value": 0.65, "weight": 15, "points": 9.8 }
  ],
  "overrides": []
}
```

## Reportes · `/reports`

| Método | Ruta | Permiso |
|---|---|---|
| GET · POST | `/reports/templates` | `report:template:manage` |
| POST | `/reports/generate` | `report:generate` |
| GET | `/reports` | `report:generate` |
| GET | `/reports/{id}` | `report:generate` |
| GET | `/reports/{id}/download?format=pdf` | `report:generate` |
| GET | `/reports/exports/cases.csv` | `report:generate` |

## Métricas de IA · `/ai`

| Método | Ruta | Permiso |
|---|---|---|
| GET | `/ai/metrics/accuracy` | `ai:metrics:read` |
| POST | `/ai/classify/preview` | `case:update:classification` |

## Códigos de estado

| Código | Uso |
|---|---|
| 200 / 201 / 204 | Éxito · creación · sin contenido |
| 400 | Cuerpo inválido (con `errors[]` por campo) |
| 401 | Sin autenticación o token vencido |
| 403 | Autenticado sin permiso |
| 404 | No existe **o está fuera del alcance del actor** |
| 409 | Conflicto de estado (transición inválida, duplicado) |
| 413 | Adjunto demasiado grande |
| 422 | Regla de negocio incumplida |
| 429 | Límite de tasa excedido |
| 500 | Error interno (nunca expone traza) |
