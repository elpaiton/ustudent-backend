# ADR-0003 · La IA vive tras un puerto, con respaldo determinista

- **Estado**: aceptado
- **Fecha**: 2026-08-13

## Contexto

La clasificación automática de solicitudes es una función central del producto, pero depende
de un servicio externo con costo por uso, latencia variable, disponibilidad no garantizada y
condiciones de tratamiento de datos que pueden cambiar. Los textos clasificados contienen
información sensible de salud mental y situación personal.

Además, es un proyecto académico: debe poder ejecutarse y evaluarse sin credenciales de
proveedor.

## Decisión

1. El dominio depende de la interfaz `CaseClassifierPort`, nunca de un SDK concreto.
2. Existen **dos adaptadores**: `LlmClassifierAdapter` (proveedor externo) y
   `RuleBasedClassifierAdapter` (léxico ponderado, local y determinista).
3. Ante error, timeout (5 s), respuesta que no valida contra el esquema, categoría fuera de
   la taxonomía o ausencia de credenciales, se **degrada automáticamente** al adaptador de
   reglas y el caso queda en `PENDING_REVIEW`.
4. La **seudonimización** ocurre en el servicio de aplicación, antes de invocar al puerto:
   ningún adaptador puede omitirla.
5. La detección de señales de urgencia se ejecuta **siempre** con el léxico local, con
   independencia del adaptador usado.
6. La clasificación es **asíncrona**: nunca está en la ruta crítica de la radicación.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| Llamar al SDK del proveedor desde el servicio | Acopla el dominio a un vendor, impide probar sin red y deja el sistema sin salida ante una caída. |
| Solo clasificador de reglas | Precisión insuficiente en texto libre y matizado, que es justamente el caso de uso. |
| Modelo propio embebido (ONNX) | No hay datos etiquetados para entrenarlo ni recursos para mantenerlo. |
| Clasificación síncrona en el POST | Rompería el RNF-P2 y ataría la disponibilidad del sistema a la del proveedor. |

## Consecuencias

**Positivas**
- El sistema funciona sin credenciales, con función degradada explícita.
- Cambiar de proveedor es escribir un adaptador nuevo.
- La red de seguridad de urgencias no depende de un tercero.
- Las pruebas usan un adaptador falso, sin red ni costo.

**Negativas**
- Hay dos rutas de clasificación que mantener y probar.
- El respaldo genera más casos en `PENDING_REVIEW`, y por tanto más trabajo humano.

**Mitigaciones**
- La bandeja destaca los `PENDING_REVIEW` para que la revisión sea rápida.
- Se monitorea el porcentaje de casos atendidos por el respaldo; si supera el 20 % de forma
  sostenida, hay un problema de integración que atender.
