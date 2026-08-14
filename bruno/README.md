# Colección de Bruno · uStudent API

Peticiones para probar la API a mano. Se versionan con el código, así que cuando un
endpoint cambia, su petición cambia en el mismo commit.

## Antes de empezar

**1. Base de datos en marcha:**

```bash
docker compose -f infra/docker/docker-compose.yml up -d
```

**2. Backend con el perfil `local`**, que es el que crea las cuentas de prueba:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Sin ese perfil la base arranca sin usuarios y ningún login funcionará.

**3. En Bruno**: *Open Collection* → elige esta carpeta (`apps/ustudent-backend/bruno`) y
selecciona el entorno **Local** en el desplegable de arriba a la derecha. Si no eliges
entorno, `{{baseUrl}}` queda vacío y todas las peticiones fallan.

## Cuentas de prueba

Todas comparten la contraseña `Admin123*`, y **solo existen con los perfiles `local` y
`dev`**.

| Correo | Rol | Para qué sirve |
|---|---|---|
| `admin@usta.edu.co` | ADMIN | 25 permisos: el flujo principal |
| `bienestar@usta.edu.co` | WELLBEING | Atención de casos |
| `docente@usta.edu.co` | TEACHER | Reportar sobre un estudiante |
| `estudiante@usta.edu.co` | STUDENT | 3 permisos: el caso más restringido |

## Cómo está organizada

**`Sistema/`** — `Ping`, para confirmar que la API responde antes de depurar otra cosa.

**`Auth/`** — el flujo completo, numerado para ejecutarse en orden:

| # | Petición | Espera |
|---|---|---|
| 01 | Me sin sesión | 401 |
| 02 | Login con datos inválidos | 400 con detalle por campo |
| 03 | Login con credenciales incorrectas | 401 |
| 04 | Login | 200 + cookies |
| 05 | Me | 200 con permisos |
| 06 | Refresh | 200 con cookies nuevas |
| 07 | Logout | 204 |
| 08 | Me después de salir | 401 |

Empieza en 01 y termina en 07 u 08: así el almacén de cookies queda limpio para la
siguiente ejecución. Si `01` te devuelve 200 en vez de 401, es que quedó una sesión
abierta — ejecuta `07 Logout` y vuelve a empezar.

**`Extras/`** — casos que no forman parte del flujo: login con otros roles para comparar
permisos, y la prueba de bloqueo por intentos, que **modifica el estado real de una
cuenta** y lo advierte en su documentación.

## Las cookies se gestionan solas

Los tokens viajan en cookies `httpOnly`, no en el cuerpo ni en cabeceras. Bruno tiene su
propio almacén de cookies: tras el login las guarda y las reenvía en las siguientes
peticiones, igual que haría un navegador.

Consecuencia práctica: **no hay que copiar ningún token a mano** ni configurar
`Authorization`. Y consecuencia menos obvia: el orden de ejecución importa, porque el
estado de la sesión persiste entre peticiones.

Si necesitas partir de cero, borra las cookies desde el propio Bruno
(*Collection settings* → *Cookies*).

## Cada petición comprueba lo suyo

Las pruebas no miran solo el código de estado; verifican lo que de verdad importa:

- Que el token **nunca** aparezca en el cuerpo de la respuesta.
- Que las cookies sean `httpOnly`.
- Que un correo inexistente y una contraseña incorrecta den la **misma** respuesta, para
  que comparándolas no se pueda averiguar qué correos están registrados.
- Que un estudiante **no** tenga `risk:read:student`: el índice de riesgo no se muestra ni
  a estudiantes ni a docentes.
- Que el 401 sea 401 y no 403.

## Pendiente

Falta la comprobación de **403 por permiso insuficiente**: hoy ningún endpoint exige un
permiso concreto, porque el CRUD de usuarios y roles llega en la siguiente entrega de la
fase 1. Cuando exista, se añade aquí una petición que intente gestionar usuarios con la
sesión del estudiante y espere 403.
