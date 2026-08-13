# uStudent · Backend

Spring Boot 4.1 · Java 21 · PostgreSQL 16. Monolito modular
([ADR-0001](../../docs/06-adr/adr-0001-monolito-modular.md)).

## Arranque local

Requiere PostgreSQL en marcha:

```bash
docker compose -f ../../infra/docker/docker-compose.yml up -d
```

```bash
./mvnw spring-boot:run
```

| Recurso | URL |
|---|---|
| Ping (publico) | http://localhost:8080/api/v1/system/ping |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Salud | http://localhost:8080/actuator/health |

Sin JDK en el `PATH`, apunta `JAVA_HOME` al que instaló IntelliJ:

```bash
JAVA_HOME=~/.jdks/corretto-21.0.10 ./mvnw spring-boot:run
```

## Estructura de paquetes

```
co.edu.usta.ustudent
├── iam/            identidad: usuarios, roles, permisos, autenticacion
├── academic/       estudiantes, docentes, programas, grupos, matriculas
├── cases/          solicitudes y reportes, seguimientos, ciclo de vida
├── ai/             clasificacion: puerto, adaptadores, seudonimizacion
├── risk/           motor de riesgo: factores, evaluaciones, modelo
├── reporting/      plantillas e informes institucionales
├── notification/   correo y avisos in-app
└── shared/         configuracion, seguridad, auditoria, errores, utilidades
```

Cada módulo de dominio repite las mismas capas:

```
<modulo>/
├── api/                controladores REST y DTO — sin logica de negocio
│   └── dto/
├── application/        casos de uso, transacciones, orquestacion
│   ├── port/           interfaces hacia servicios externos
│   └── service/
├── domain/             entidades, objetos de valor, reglas invariantes
│   ├── model/
│   └── repository/     interfaces de repositorio
└── infrastructure/     implementaciones: JPA, clientes HTTP, adaptadores
    ├── persistence/
    └── adapter/
```

## Reglas que verifica ArchUnit

1. `api → application → domain`; `infrastructure → domain`. Nunca al revés.
2. Ningún módulo depende del `domain` ni de la `infrastructure` de otro.
3. El paquete `domain` no importa Spring ni JPA.
4. `shared` no conoce ningún módulo de dominio.
5. Sin ciclos entre módulos: `cases` y `ai` se comunican por eventos.

Están en `src/test/java/.../architecture/`. Romper una hace fallar el build.

## Comandos

```bash
./mvnw -Dtest=ArchitectureTest,ModuleBoundaryTest -DfailIfNoSpecifiedTests=false test
```

```bash
./mvnw verify
```

`verify` necesita Docker: Testcontainers levanta PostgreSQL 16 para las pruebas de
integración.

## Convenciones

- **Migraciones**: `src/main/resources/db/migration/V<n>__<descripcion>.sql`. Una migración
  aplicada no se edita jamás; se corrige con una nueva.
- **Semillas** (permisos, roles, modelo de riesgo, catálogos): `R__seed_*.sql`, repetibles e
  idempotentes. `R__seed_roles_permissions.sql` es la traducción directa de la
  [matriz de permisos](../../docs/01-requisitos/matriz-roles-permisos.md).
- **Transacciones**: solo en `application/service`. Nunca en controladores ni repositorios.
- **Eventos**: `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- **Autorización**: `@PreAuthorize("hasAuthority('recurso:accion')")` en el servicio de
  aplicación, más filtro de alcance en la consulta.
- **Errores**: excepciones de dominio (`ApiException`) traducidas a RFC 7807 por
  `GlobalExceptionHandler`. Toda respuesta de error lleva `traceId`.

## Notas sobre Spring Boot 4

El proyecto usa la línea 4.x, que renombró varios artefactos respecto a la 3.x:

| Antes (Boot 3) | Ahora (Boot 4) |
|---|---|
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| `spring-boot-starter-test` | Starters por módulo: `...-webmvc-test`, `...-data-jpa-test`, etc. |
| `springdoc-openapi` 2.x | `springdoc-openapi` 3.x (la 2.x no es compatible) |

Al buscar documentación, verifica que corresponda a la línea 4.x: mucho material en línea
sigue asumiendo la 3.x.

## Referencias

- [Vista de arquitectura](../../docs/02-arquitectura/vision-arquitectura.md)
- [Modelo de datos](../../docs/02-arquitectura/modelo-datos.md)
- [Contrato de API](../../docs/03-especificaciones/api/api-rest.md)
- [Motor de riesgo](../../docs/03-especificaciones/reglas/motor-riesgo-desercion.md)
