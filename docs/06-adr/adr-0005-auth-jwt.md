# ADR-0005 · Autenticación con JWT propia y RBAC por permisos atómicos

- **Estado**: aceptado
- **Fecha**: 2026-08-13

## Contexto

Cinco tipos de usuario con capacidades muy distintas y una promesa explícita del producto:
que el administrador pueda **gestionar roles y permisos** sin intervención de desarrollo. El
frontend es Next.js con renderizado en servidor, lo que condiciona dónde vive la sesión.
No hay, por ahora, un proveedor de identidad institucional disponible para integrar.

## Decisión

1. **JWT firmado con RS256** emitido por el backend, con acceso de 30 min y refresco
   rotatorio de 8 h.
2. Los tokens viajan en **cookies `httpOnly`, `Secure`, `SameSite=Lax`**, no en
   `localStorage`: así los Server Components pueden leerlos y el token queda fuera del
   alcance de JavaScript.
3. El token transporta `roles` y **`perms`** (permisos efectivos), de modo que la
   autorización no consulte la base de datos en cada petición.
4. La autorización se expresa **siempre por permiso atómico** (`case:assign`), nunca por rol.
   Crear un rol nuevo es un cambio de datos.
5. La vida corta del acceso (30 min) acota la ventana de permisos desactualizados; además,
   cambiar roles o desactivar un usuario **revoca** sus tokens vigentes mediante una lista de
   `jti` revocados en caché.
6. El refresco rota con detección de reutilización: usar un refresco ya consumido invalida
   toda la familia.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| Sesión en servidor (`JSESSIONID`) | Obliga a estado compartido si algún día hay más de una instancia, y complica el SSR y las pruebas. |
| Keycloak / proveedor OIDC | Sería la mejor opción si existiera integración institucional disponible; hoy añade un componente que operar. **Reevaluar** cuando la universidad exponga SSO. |
| JWT en `localStorage` | Expuesto a XSS y no legible desde Server Components. |
| Roles en las anotaciones (`hasRole('ADMIN')`) | Cada rol nuevo exigiría cambiar código y desplegar, rompiendo un requisito del producto. |

## Consecuencias

**Positivas**
- Backend sin estado, escalable horizontalmente sin sesiones compartidas.
- Autorización sin viaje a la base de datos por petición.
- Roles y permisos administrables en caliente.
- Los Server Components de Next.js leen la cookie directamente.

**Negativas**
- Un cambio de permisos tarda hasta 30 min en propagarse, salvo revocación explícita.
- Un token con muchos permisos crece; hay que vigilar el tamaño de la cookie.
- Gestionar la rotación de claves de firma es responsabilidad del equipo.

**Mitigaciones**
- Lista de revocación en caché con TTL igual a la vida del token, para los casos que no
  pueden esperar (desactivación, cambio de rol).
- Los permisos se serializan por código corto; si el token superara los 4 KB, se pasaría a
  enviar solo los roles y a resolver los permisos desde caché en memoria.
- Procedimiento de rotación de claves documentado en [operación](../07-operacion/entornos-despliegue.md).

## Cuándo revisar

En cuanto la universidad ofrezca SSO institucional (OIDC/SAML). El diseño lo prevé: el
`AuthService` es la única pieza que cambiaría, porque el resto del sistema depende de los
permisos, no de cómo se autenticó la persona.
