# Modelo de seguridad

## Autenticación

JWT emitido por el backend ([ADR-0005](../06-adr/adr-0005-auth-jwt.md)).

| Token | Vida | Almacenamiento | Contenido |
|---|---|---|---|
| Acceso | 30 min | Cookie `httpOnly`, `Secure`, `SameSite=Lax` | `sub` (public_id), `roles`, `perms`, `exp`, `jti` |
| Refresco | 8 h | Cookie `httpOnly` + registro en BD | `sub`, `jti`, familia de rotación |

- Firma **RS256**; la clave privada solo existe en el servidor y se rota por configuración.
- El refresco **rota**: usar un token de refresco ya consumido invalida toda la familia
  (detección de robo).
- Cerrar sesión, desactivar el usuario o cambiar sus roles revoca sus tokens vigentes
  (lista de `jti` revocados en caché con TTL igual a la vida del token).
- Bloqueo tras 5 intentos fallidos durante 15 minutos, contado por usuario **y** por IP.

## Autorización

Tres capas, todas obligatorias:

1. **Permiso** — `@PreAuthorize("hasAuthority('case:assign')")` en el servicio de aplicación.
2. **Alcance de fila** — el repositorio recibe el contexto del actor y filtra por las
   [reglas R1–R7](../01-requisitos/matriz-roles-permisos.md). Nunca se filtra en el cliente.
3. **Regla de dominio** — invariantes que no dependen del actor (transiciones válidas de
   estado, campos obligatorios al cerrar).

> El frontend oculta lo que el usuario no puede usar. Ocultar **no** es autorizar: cada
> endpoint verifica por su cuenta.

## Protección de datos sensibles

| Dato | Tratamiento |
|---|---|
| `cas_cases.description`, `cas_follow_ups.note`, `rsk_mood_checkins.comment` | Cifrado en reposo AES-256-GCM con clave gestionada fuera del repositorio; conversor JPA transparente |
| Contraseñas | BCrypt coste 12; nunca se devuelven ni se registran |
| Adjuntos | Fuera de la BD, con nombre aleatorio, servidos por URL firmada de vida corta |
| Texto enviado a la IA | Seudonimizado ([detalle](integracion-ia.md)) |
| Logs | Filtro que enmascara documentos, correos y `Authorization`; prohibido registrar cuerpos de caso |

## Auditoría

Tabla `sys_audit_log` **append-only**: el usuario de la aplicación tiene `INSERT` y
`SELECT`, no `UPDATE` ni `DELETE`.

Se audita, como mínimo: inicio y cierre de sesión, fallos de autenticación, lectura de
expediente ajeno, lectura de caso confidencial, cambios de rol y permiso, cambios del
modelo de riesgo, generación y exportación de reportes, y toda llamada al proveedor de IA.

## Superficie de ataque y controles

| Amenaza | Control |
|---|---|
| Fuerza bruta de credenciales | Bloqueo por usuario e IP, retraso progresivo |
| Enumeración de usuarios | Respuesta y tiempo idénticos para usuario inexistente y contraseña errónea |
| IDOR (acceso por id ajeno) | `public_id` UUID + verificación de alcance en cada consulta, nunca solo por id |
| Inyección SQL | Consultas parametrizadas; prohibida la concatenación de SQL |
| XSS | React escapa por defecto; `dangerouslySetInnerHTML` prohibido; CSP estricta |
| CSRF | Cookies `SameSite=Lax` + token anti-CSRF en mutaciones |
| Carga de archivos maliciosos | Lista blanca de tipos (pdf, jpg, png, docx), límite 10 MB, verificación de firma binaria, sin ejecución |
| Inyección de instrucciones vía texto del caso | El texto del usuario va como dato en un bloque delimitado; la respuesta se valida contra esquema y taxonomía cerrada; ninguna salida de la IA ejecuta acciones fuera de clasificar |
| Escalada de privilegios | Los permisos vienen del token firmado, jamás del cuerpo de la petición; un admin no puede autoasignarse permisos sin quedar auditado |
| Exposición de datos en errores | RFC 7807 sin trazas; el detalle técnico solo va al log con `traceId` |

## Cabeceras de seguridad

```
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'; frame-ancestors 'none'; object-src 'none'
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), camera=(), microphone=()
```

CORS restringido al origen del frontend por entorno; nunca `*` con credenciales.

## Gestión de secretos

- Ningún secreto en el repositorio. `.env.example` documenta las variables; `.env` está ignorado.
- En producción, variables de entorno inyectadas por el orquestador o gestor de secretos.
- Rotación de la clave de firma JWT y de la clave de cifrado documentada en
  [operación](../07-operacion/entornos-despliegue.md).

## Verificación

- Análisis de dependencias (OWASP Dependency-Check) en cada build.
- Revisión del checklist OWASP ASVS nivel 1 antes de cada entrega mayor.
- Pruebas automatizadas de autorización: por cada endpoint, un test que confirma el 403 con
  rol insuficiente. Sin ese test, el endpoint no se considera terminado
  ([DoD](../08-plan/definicion-de-terminado.md)).
