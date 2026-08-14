# Infraestructura

## Contenido

```
infra/
├── docker/
│   └── docker-compose.yml   PostgreSQL 16 + Mailpit (+ MinIO opcional)
└── db/
    ├── init/                scripts de inicialización del contenedor (extensiones)
    └── seed/                datos de demostración para desarrollo
```

## Levantar el entorno local

```bash
docker compose -f infra/docker/docker-compose.yml up -d
```

| Servicio | Puerto | Notas |
|---|---|---|
| PostgreSQL | 5432 | Base `ustudent`, usuario `ustudent` |
| Mailpit (SMTP) | 1025 | El backend envía aquí en local |
| Mailpit (web) | 8025 | Bandeja de correo de prueba |

Las migraciones **no** se aplican aquí: las ejecuta Flyway al arrancar el backend. Los
scripts de `db/init` solo crean extensiones (`citext`, `pg_trgm`).

## Detener y limpiar

```bash
docker compose -f infra/docker/docker-compose.yml down
```

Para borrar también los datos (irreversible):

```bash
docker compose -f infra/docker/docker-compose.yml down -v
```

## Producción

El despliegue, las variables de entorno, los respaldos y la rotación de claves están en
[`docs/07-operacion/entornos-despliegue.md`](../docs/07-operacion/entornos-despliegue.md).
