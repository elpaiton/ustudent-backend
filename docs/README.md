# Documentación de uStudent

Fuente de verdad del diseño del sistema. Todo documento aquí es versionable, revisable y
debe actualizarse **antes** de que el código lo contradiga.

Cubre **las dos aplicaciones**, aunque viva en el repositorio del backend: el código de la
interfaz está en [ustudent-frontend](https://github.com/elpaiton/ustudent-frontend), pero su
sistema de diseño y su mapa de navegación se documentan aquí, junto al resto.

## Índice

### 00 · Visión
- [Visión y alcance](00-vision/vision-alcance.md) — problema, objetivos, alcance, fuera de alcance
- [Glosario](00-vision/glosario.md) — lenguaje ubicuo del dominio

### 01 · Requisitos
- [Historias de usuario](01-requisitos/historias-usuario.md) — épicas e historias con criterios de aceptación
- [Requisitos no funcionales](01-requisitos/requisitos-no-funcionales.md) — rendimiento, seguridad, privacidad
- [Matriz de roles y permisos](01-requisitos/matriz-roles-permisos.md) — RBAC detallado

### 02 · Arquitectura
- [Vista de arquitectura](02-arquitectura/vision-arquitectura.md) — estilo, capas, módulos, decisiones transversales
- [Modelo de datos](02-arquitectura/modelo-datos.md) — entidades, relaciones, reglas de integridad
- [Modelo de seguridad](02-arquitectura/seguridad.md) — autenticación, autorización, datos sensibles
- [Integración de IA](02-arquitectura/integracion-ia.md) — puerto de clasificación, proveedores, fallback

### 03 · Especificaciones
- [API REST](03-especificaciones/api/api-rest.md) — contrato de endpoints por módulo
- [Motor de riesgo de deserción](03-especificaciones/reglas/motor-riesgo-desercion.md) — factores, pesos, fórmula
- [Taxonomía de clasificación](03-especificaciones/reglas/taxonomia-clasificacion.md) — categorías y prioridades
- [Plantilla de reporte general](03-especificaciones/plantillas/reporte-general.md) — estructura del informe institucional

### 04 · Diagramas
- [C4 contexto](04-diagramas/c4/c1-contexto.md) · [C4 contenedores](04-diagramas/c4/c2-contenedores.md) · [C4 componentes](04-diagramas/c4/c3-componentes-backend.md)
- [Secuencia: radicar solicitud](04-diagramas/secuencia/radicar-solicitud.md)
- [Secuencia: cálculo de riesgo](04-diagramas/secuencia/calculo-riesgo.md)
- [Estados de un caso](04-diagramas/estados/ciclo-vida-caso.md)
- [Modelo entidad-relación](04-diagramas/entidad-relacion/mer.md)

### 05 · UX
- [Sistema de diseño](05-ux/sistema-diseno.md) — paleta, tipografía, tokens, componentes
- [Mapa de navegación](05-ux/mapa-navegacion.md) — pantallas por rol

### 06 · Decisiones de arquitectura (ADR)
- [ADR-0001 Monolito modular](06-adr/adr-0001-monolito-modular.md)
- [ADR-0002 PostgreSQL como almacén único](06-adr/adr-0002-postgresql.md)
- [ADR-0003 IA tras un puerto con fallback determinista](06-adr/adr-0003-ia-tras-puerto.md)
- [ADR-0004 Riesgo por reglas configurables, no por modelo entrenado](06-adr/adr-0004-riesgo-por-reglas.md)
- [ADR-0005 Autenticación con JWT propia](06-adr/adr-0005-auth-jwt.md)

### 07 · Operación
- [Entornos y despliegue](07-operacion/entornos-despliegue.md)
- [Observabilidad](07-operacion/observabilidad.md)

### 08 · Plan
- [Roadmap por fases](08-plan/roadmap.md) — **empezar aquí para desarrollar**
- [Backlog inicial](08-plan/backlog.md)
- [Definición de terminado](08-plan/definicion-de-terminado.md)

### 09 · Calidad
- [Estrategia de pruebas](09-calidad/estrategia-pruebas.md)

## Convenciones

- Diagramas en **Mermaid** dentro de Markdown — se renderizan en GitHub y no requieren binarios.
- Documentos en español; identificadores de código, endpoints y nombres de tabla en inglés.
- Cada decisión estructural discutible se registra como ADR; los ADR no se editan, se suceden.
