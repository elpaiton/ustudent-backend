# Backlog inicial

Tareas derivadas del [roadmap](roadmap.md), listas para volcarse a un tablero. Estimación en
puntos relativos (1 ≈ media jornada, 8 ≈ una semana de una persona).

## Fase 0 · Cimientos

| ID | Tarea | Pts | Depende de |
|---|---|---|---|
| T0.1 | Inicializar Git, `.gitignore`, `.editorconfig`, convención de commits | 1 | — |
| T0.2 | Esqueleto Spring Boot con módulos y `pom.xml` | 3 | T0.1 |
| T0.3 | Esqueleto Next.js con Tailwind y tokens de diseño | 3 | T0.1 |
| T0.4 | `docker-compose`: PostgreSQL + MailHog | 2 | T0.1 |
| T0.5 | Flyway + `V1__initial_schema.sql` | 2 | T0.2, T0.4 |
| T0.6 | Errores RFC 7807 + OpenAPI + CORS | 2 | T0.2 |
| T0.7 | Reglas ArchUnit de fronteras de módulo | 2 | T0.2 |
| T0.8 | Pipeline de CI | 3 | T0.2, T0.3 |

## Fase 1 · Identidad y acceso

| ID | Tarea | Pts | Depende de |
|---|---|---|---|
| T1.1 | Entidades `iam` + migración | 3 | T0.5 |
| T1.2 | Emisión y verificación de JWT RS256 | 3 | T1.1 |
| T1.3 | Login, refresco rotatorio, logout, bloqueo por intentos | 5 | T1.2 |
| T1.4 | `PermissionEvaluator` + `@PreAuthorize` | 3 | T1.2 |
| T1.5 | Semilla de roles y permisos | 2 | T1.1 |
| T1.6 | CRUD de usuarios + asignación de roles | 5 | T1.4 |
| T1.7 | CRUD de roles + matriz de permisos | 5 | T1.4 |
| T1.8 | Componentes base del sistema de diseño | 5 | T0.3 |
| T1.9 | Pantalla de login + gestión de sesión | 3 | T1.3, T1.8 |
| T1.10 | Layout autenticado + menú por permisos | 5 | T1.9 |
| T1.11 | Pantallas de administración de usuarios y roles | 5 | T1.6, T1.7, T1.10 |
| T1.12 | Pruebas de autorización de todos los endpoints | 3 | T1.6, T1.7 |

## Fase 2 · Casos · radicación

| ID | Tarea | Pts | Depende de |
|---|---|---|---|
| T2.1 | Entidades `academic` + migración | 3 | T1.1 |
| T2.2 | Importación CSV con validación y reporte de errores | 5 | T2.1 |
| T2.3 | Entidad `Case` + máquina de estados + historial | 5 | T2.1 |
| T2.4 | `POST /cases` con idempotencia y numeración | 5 | T2.3 |
| T2.5 | Cifrado en reposo de campos sensibles | 3 | T2.3 |
| T2.6 | Listados con filtros, paginación y alcance por rol | 5 | T2.4 |
| T2.7 | Adjuntos: carga, validación, almacenamiento | 5 | T2.4 |
| T2.8 | Frontend estudiante: nueva solicitud | 5 | T2.4, T1.10 |
| T2.9 | Frontend estudiante: listado y detalle | 5 | T2.6 |
| T2.10 | Frontend docente: buscador con alcance + reporte corto | 5 | T2.6 |
| T2.11 | Auditoría de acceso a casos | 3 | T2.6 |
| T2.12 | Recuperación de contraseña | 3 | T1.3 |

## Fase 3 · Casos · atención

| ID | Tarea | Pts | Depende de |
|---|---|---|---|
| T3.1 | Endpoint de transiciones con validación de estados | 3 | T2.3 |
| T3.2 | Asignación y reasignación (individual y por lotes) | 3 | T3.1 |
| T3.3 | Seguimientos con visibilidad configurable | 3 | T2.3 |
| T3.4 | Expediente consolidado del estudiante | 5 | T3.3 |
| T3.5 | Módulo `notification` + `sys_async_jobs` | 5 | T2.4 |
| T3.6 | Plantillas de correo | 2 | T3.5 |
| T3.7 | Frontend: bandeja con filtros | 5 | T3.1 |
| T3.8 | Frontend: detalle del caso con acciones y línea de tiempo | 8 | T3.7 |
| T3.9 | Frontend: expediente del estudiante | 5 | T3.4 |
| T3.10 | Consulta de auditoría (admin) | 3 | T2.11 |

## Fase 4 · IA y ánimo

| ID | Tarea | Pts | Depende de |
|---|---|---|---|
| T4.1 | `CaseClassifierPort` + adaptador de reglas + léxico | 5 | T2.3 |
| T4.2 | `Pseudonymizer` con pruebas de casos límite | 3 | — |
| T4.3 | Prompt versionado + validación por esquema | 3 | T4.1 |
| T4.4 | `LlmClassifierAdapter` con timeout, reintento y disyuntor | 5 | T4.3 |
| T4.5 | `JobRunner` + clasificación asíncrona | 5 | T3.5 |
| T4.6 | Señales de urgencia + escalada automática | 5 | T4.1 |
| T4.7 | Persistencia sugerencia vs. decisión humana | 3 | T4.5 |
| T4.8 | Frontend: sugerencia con confianza, aceptar/corregir | 3 | T4.7, T3.8 |
| T4.9 | Check-in de ánimo: modelo + endpoint | 3 | T2.1 |
| T4.10 | Frontend: `MoodScale` + historial | 5 | T4.9 |
| T4.11 | Conjunto de 40 casos etiquetados para medir precisión | 3 | T4.1 |

## Fase 5 · Motor de riesgo

| ID | Tarea | Pts | Depende de |
|---|---|---|---|
| T5.1 | Modelo y factores + semilla v1.0.0 | 3 | T2.1 |
| T5.2 | `RiskInputLoader` | 5 | T5.1 |
| T5.3 | `RiskEngine` puro + 8 evaluadores | 8 | T5.2 |
| T5.4 | Reglas de anulación OV-1..OV-4 | 3 | T5.3 |
| T5.5 | Persistencia con desglose e histórico | 3 | T5.3 |
| T5.6 | Disparadores (caso, check-in, job diario, manual) | 5 | T5.5 |
| T5.7 | Alertas por cambio de nivel | 3 | T5.6, T3.5 |
| T5.8 | Panel de parámetros con versionado | 5 | T5.1 |
| T5.9 | Frontend: `RiskBreakdown` en el expediente | 5 | T5.5 |
| T5.10 | Frontend: tablero de riesgo con gráficas | 8 | T5.5 |
| T5.11 | Pruebas del motor al 100 % | 5 | T5.4 |

## Fase 6 · Reportes

| ID | Tarea | Pts | Depende de |
|---|---|---|---|
| T6.1 | Plantillas versionadas + gestión | 5 | T1.1 |
| T6.2 | Consultas agregadas del informe | 8 | T5.5 |
| T6.3 | Umbral de anonimato en agregados | 2 | T6.2 |
| T6.4 | Render Thymeleaf → PDF | 5 | T6.1, T6.2 |
| T6.5 | Exportación CSV | 3 | T6.2 |
| T6.6 | Frontend: generar, listar y descargar informes | 5 | T6.4 |
| T6.7 | Tablero de precisión del clasificador | 5 | T4.7 |

## Fase 7 · Endurecimiento

| ID | Tarea | Pts |
|---|---|---|
| T7.1 | Prueba de carga k6 + ajuste de índices | 5 |
| T7.2 | Revisión OWASP ASVS nivel 1 | 5 |
| T7.3 | Auditoría de accesibilidad AA | 3 |
| T7.4 | E2E Playwright de recorridos críticos | 5 |
| T7.5 | Manual de usuario por rol | 5 |
| T7.6 | Datos de demostración sintéticos | 3 |
| T7.7 | Calibración del motor contra retiros reales | 5 |
| T7.8 | Preparación de entrega | 3 |

## Ruta crítica

`T0.2 → T0.5 → T1.1 → T1.2 → T1.4 → T2.1 → T2.3 → T2.4 → T5.2 → T5.3 → T5.5 → T6.2 → T6.4`

Todo lo que no está en esta ruta puede paralelizarse. El frontend de una fase puede
adelantarse contra el contrato OpenAPI antes de que el backend esté listo.
