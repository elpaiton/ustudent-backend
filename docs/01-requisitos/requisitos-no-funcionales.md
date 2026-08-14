# Requisitos no funcionales

## Rendimiento

| Id | Requisito | Verificación |
|---|---|---|
| RNF-P1 | p95 de las peticiones de lectura < 400 ms con 200 usuarios concurrentes | Prueba de carga con k6 sobre el listado de casos y el tablero de riesgo |
| RNF-P2 | La radicación de un caso responde en < 1 s; la clasificación por IA ocurre después, de forma asíncrona | La respuesta HTTP no espera al proveedor de IA |
| RNF-P3 | El recálculo diario de riesgo de 20 000 estudiantes termina en < 15 min | Job por lotes con paginación, medido en preproducción |
| RNF-P4 | El reporte institucional se genera en < 60 s | Consultas agregadas precalculadas por período |

## Escalabilidad

| Id | Requisito |
|---|---|
| RNF-E1 | Dimensionado objetivo: 20 000 estudiantes, 1 500 docentes, 40 000 casos/año |
| RNF-E2 | El backend es *stateless*: la sesión vive en el token, no en memoria; permite N instancias tras un balanceador aunque se despliegue una sola |
| RNF-E3 | Los adjuntos no se guardan en la base de datos, sino en almacenamiento de objetos o volumen dedicado |

## Seguridad y privacidad

| Id | Requisito |
|---|---|
| RNF-S1 | Toda comunicación por HTTPS; HSTS activo en producción |
| RNF-S2 | Contraseñas con BCrypt (coste ≥ 12); nunca en logs ni en respuestas |
| RNF-S3 | Autorización por permiso atómico verificada en el servidor en cada endpoint; el frontend solo oculta, nunca autoriza |
| RNF-S4 | La descripción del caso y las notas de seguimiento se cifran en reposo (AES-256, clave fuera del repositorio) |
| RNF-S5 | Todo acceso a un expediente queda auditado con usuario, recurso, fecha e IP |
| RNF-S6 | Los datos de menores de edad y de salud mental solo son accesibles por roles del área correspondiente |
| RNF-S7 | Retención: los casos cerrados se conservan 5 años; después se anonimizan conservando solo agregados |
| RNF-S8 | El sistema pide consentimiento informado al estudiante en el primer ingreso y lo registra con fecha y versión del texto |
| RNF-S9 | Los textos enviados al proveedor de IA se seudonimizan: se eliminan nombres, documentos y correos antes del envío |

## Disponibilidad y continuidad

| Id | Requisito |
|---|---|
| RNF-D1 | Disponibilidad objetivo 99 % en horario académico (6:00–22:00) |
| RNF-D2 | Respaldo diario de base de datos con retención de 30 días y prueba de restauración mensual |
| RNF-D3 | La caída del proveedor de IA degrada la función, no el servicio (clasificador de respaldo) |

## Usabilidad y accesibilidad

| Id | Requisito |
|---|---|
| RNF-U1 | WCAG 2.1 nivel AA: contraste ≥ 4.5:1 en texto, foco visible, navegación completa por teclado |
| RNF-U2 | Responsive desde 360 px de ancho |
| RNF-U3 | El reporte docente de inasistencia se completa en ≤ 4 interacciones |
| RNF-U4 | Todo mensaje de error indica qué pasó y qué hacer; nunca expone trazas técnicas |

## Mantenibilidad

| Id | Requisito |
|---|---|
| RNF-M1 | Cobertura de pruebas ≥ 70 % en el backend y 100 % de las reglas del motor de riesgo |
| RNF-M2 | Las dependencias entre módulos del monolito se verifican automáticamente con ArchUnit |
| RNF-M3 | El esquema de base de datos evoluciona solo por migraciones versionadas (Flyway) |
| RNF-M4 | La API está documentada con OpenAPI generado desde el código |
| RNF-M5 | Los pesos y umbrales del motor de riesgo son datos, no código |

## Cumplimiento

| Id | Requisito |
|---|---|
| RNF-C1 | Tratamiento de datos conforme a la Ley 1581 de 2012 (Colombia) y su política institucional |
| RNF-C2 | El estudiante puede solicitar copia de sus datos y su rectificación |
| RNF-C3 | La plataforma no emite diagnóstico clínico ni sustituye la valoración profesional; el texto lo advierte explícitamente en la interfaz |
