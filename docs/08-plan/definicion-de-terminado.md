# Definición de terminado (DoD)

Una tarea está terminada cuando cumple **todo** lo que sigue. No hay «terminado salvo las
pruebas».

## Toda tarea

- [ ] El código compila y el pipeline de CI está verde.
- [ ] Sin advertencias nuevas del compilador ni del linter.
- [ ] Formato aplicado (Spotless en el backend, Prettier en el frontend).
- [ ] Revisada por otra persona del equipo.
- [ ] La documentación afectada está actualizada **en el mismo PR**.

## Backend

- [ ] Migración Flyway versionada, aplicada y verificada; ninguna migración previa editada.
- [ ] Reglas de dominio con pruebas unitarias, incluidos los casos límite.
- [ ] Prueba de integración con Testcontainers para el recorrido principal.
- [ ] **Prueba de autorización**: existe un test que confirma 403 con rol insuficiente para
      cada endpoint nuevo. Sin ese test el endpoint no está terminado.
- [ ] Alcance por fila verificado con prueba cuando el endpoint devuelve datos de terceros.
- [ ] Endpoint documentado en OpenAPI, con ejemplos de petición y respuesta.
- [ ] Errores en RFC 7807 con código propio; ninguna traza expuesta.
- [ ] Sin datos sensibles en logs.
- [ ] Acciones sensibles anotadas con `@Audited`.
- [ ] Las fronteras de módulo se respetan (ArchUnit verde).

## Frontend

- [ ] Tipos generados desde OpenAPI, no escritos a mano.
- [ ] Estados de **carga**, **error** y **vacío** implementados; nada de pantalla en blanco.
- [ ] Responsive verificado a 360 px, 768 px y 1280 px.
- [ ] Accesible: navegable por teclado, foco visible, etiquetas asociadas, contraste AA.
- [ ] Sin colores en duro: solo tokens del sistema de diseño.
- [ ] Los formularios validan en el cliente **y** manejan los errores del servidor por campo.
- [ ] Ningún dato sensible se muestra fuera del alcance del rol (verificado a mano por rol).
- [ ] Textos en español, con el tono definido en el sistema de diseño.

## Motor de riesgo (adicional)

- [ ] La regla está cubierta al 100 % con tabla de casos.
- [ ] Es determinista: no lee el reloj ni la base de datos dentro del motor.
- [ ] Aporta su desglose al resultado.
- [ ] Documentada en la especificación del motor con su fórmula de normalización.

## Integración con IA (adicional)

- [ ] El adaptador degrada correctamente ante fallo, y hay una prueba que lo demuestra.
- [ ] La seudonimización se verifica con prueba antes de cualquier llamada externa.
- [ ] La respuesta se valida contra el esquema y la taxonomía cerrada.
- [ ] Las pruebas no llaman al proveedor real: usan un adaptador falso.

## Definición de listo (DoR) — antes de empezar

- [ ] La historia tiene criterios de aceptación verificables.
- [ ] Se conocen los permisos que exige.
- [ ] Se sabe qué se audita.
- [ ] El diseño de la pantalla está acordado, al menos como boceto.
- [ ] Las dependencias con otras tareas están identificadas.
