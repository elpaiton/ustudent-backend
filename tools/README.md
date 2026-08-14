# Herramientas de apoyo

Scripts que no forman parte del producto pero sostienen el desarrollo.

```
tools/
└── scripts/
    ├── generate-api-types      OpenAPI del backend → src/types/api.ts del frontend
    ├── seed-demo               Datos sintéticos coherentes para desarrollo y demos
    ├── anonymize-dump          Volcado de producción → datos anonimizados para staging
    ├── check-contrast          Verifica el contraste de los pares de tokens de color
    └── gen-keys                Genera el par RSA para firmar los JWT
```

## Notas

- `seed-demo` genera **solo datos sintéticos**: 200 estudiantes, 20 docentes, 5 programas y
  ~400 casos repartidos por categoría y en el tiempo, para que el tablero de riesgo tenga
  forma realista. Nunca datos reales, ni siquiera parciales.
- `anonymize-dump` es obligatorio antes de llevar cualquier corte de producción a `staging`.
- `check-contrast` corre en CI: si un par de tokens queda por debajo del mínimo, el build
  falla ([RNF-U1](https://github.com/elpaiton/ustudent-docs/blob/main/01-requisitos/requisitos-no-funcionales.md)).
- `gen-keys` produce claves **solo para desarrollo local**. Las de producción se generan y
  custodian en el gestor de secretos.
