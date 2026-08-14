# ADR-0004 · Riesgo de deserción por reglas configurables, no por modelo entrenado

- **Estado**: aceptado
- **Fecha**: 2026-08-13

## Contexto

El producto promete un «porcentaje de deserción» por estudiante. La tentación natural es
entrenar un clasificador con datos históricos. Pero:

- No existe un conjunto de datos etiquetado: el sistema apenas empieza a registrar casos, y
  `withdrawn_at` solo se poblará al cerrar los primeros períodos.
- El resultado afecta a personas: activa contacto institucional y puede estigmatizar. Debe
  ser **explicable** ante el estudiante, ante bienestar y ante un comité académico.
- El área de bienestar tiene criterio experto que un modelo sin datos no puede superar, y
  ese criterio cambia entre períodos y programas.

## Decisión

Un **motor de reglas de suma ponderada** con factores normalizados, pesos y umbrales
**almacenados como datos versionados** en base de datos y editables desde el panel de
administración.

- Cada evaluación persiste el aporte de cada factor.
- Cada evaluación guarda la versión del modelo usada; cambiar los pesos no reescribe el pasado.
- El motor es puro y determinista, y su cobertura de pruebas es del 100 %.
- El resultado se comunica como **índice de priorización**, nunca como probabilidad.

## Alternativas consideradas

| Alternativa | Por qué no (todavía) |
|---|---|
| Regresión logística sobre históricos | Requiere al menos 2–3 períodos con retiros etiquetados. Es el paso siguiente natural, no el primero. |
| Árbol de decisión / gradient boosting | Más precisión potencial, menor explicabilidad por caso individual, y el mismo problema de datos. |
| Puntaje fijo en código | Imposible de ajustar por el área de bienestar sin desplegar; convierte una decisión institucional en una decisión de desarrollo. |
| Pedirle el puntaje al LLM | No auditable, no reproducible, no determinista. Inaceptable para una decisión que afecta a personas. |

## Consecuencias

**Positivas**
- Cada puntaje se explica factor por factor, con números verificables.
- El área de bienestar ajusta el modelo sin depender del equipo de desarrollo.
- Es reproducible y comprobable: la misma entrada siempre da la misma salida.
- Sirve de línea base contra la cual medir cualquier modelo futuro.

**Negativas**
- Los pesos iniciales son un juicio experto, no una estimación empírica.
- Puede tener sesgo: solo ve lo que se reporta; quien no pide ayuda no aparece.
- No captura interacciones no lineales entre factores.

**Mitigaciones**
- El factor `MOOD_SILENCE` penaliza la ausencia de señales, mitigando parcialmente el sesgo
  de subregistro.
- La marca `insufficientData` distingue «sin señales» de «sin riesgo».
- **Calibración obligatoria**: al cierre del primer período completo se contrasta el puntaje
  contra `withdrawn_at` real y se ajustan pesos. Es el entregable de cierre de la fase 7.

## Cuándo revisar

Cuando existan dos períodos completos con retiros registrados y al menos 300 casos
etiquetados. En ese punto se evalúa una regresión logística **contra** el motor de reglas
como línea base, exigiéndole superarla en recall con explicabilidad comparable.
