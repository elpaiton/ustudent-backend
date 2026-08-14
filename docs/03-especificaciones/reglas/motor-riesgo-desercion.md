# Motor de riesgo de deserción

Convierte señales observables en un **puntaje 0–100** y un **nivel de alerta**, de forma
explicable y parametrizable. No es un modelo entrenado
([ADR-0004](../../06-adr/adr-0004-riesgo-por-reglas.md)): es una suma ponderada de factores
normalizados, cuyos pesos edita el administrador desde el panel.

## Fórmula

Para el estudiante *e* en el período activo, con el modelo activo *M*:

```
score(e) = clamp( 100 × Σ ( wᵢ × fᵢ(e) ) / Σ wᵢ , 0 , 100 )
```

- `fᵢ(e)` ∈ [0, 1] — valor **normalizado** del factor *i*. 0 = sin señal de riesgo, 1 = señal máxima.
- `wᵢ` — peso del factor en el modelo activo.
- Los factores deshabilitados salen del numerador **y** del denominador: apagar un factor no
  diluye el puntaje, lo redistribuye.

Cada factor aporta `puntos = 100 × wᵢ × fᵢ / Σw`, y ese desglose se persiste en
`rsk_assessments.factor_breakdown`.

## Factores de la versión 1

| Código | Factor | Fuente | Normalización `f` | Peso por defecto |
|---|---|---|---|---|
| `ATTENDANCE` | Inasistencia reportada | Casos de categoría `ATTENDANCE` en los últimos 60 días | `min(sesiones_faltadas / 8, 1)` | 20 |
| `PSYCH_LOAD` | Carga psicológica | Casos `PSYCHOLOGICAL` abiertos o cerrados < 90 días | `min(nº casos / 3, 1)`, +0.3 si alguno fue `CRITICAL` | 20 |
| `MOOD_TREND` | Tendencia de ánimo | Media de `mood_checkins` de 30 días | `(5 − media) / 4`, ponderando doble la última semana | 15 |
| `MOOD_SILENCE` | Ausencia de check-ins | Días desde el último check-in | `min(días_sin_checkin / 45, 1)` | 5 |
| `FINANCIAL` | Señal económica | Casos `FINANCIAL` en el período | `min(nº casos / 2, 1)` | 15 |
| `ACADEMIC` | Señal académica | Casos `ACADEMIC` + semestre repetido | `min(nº casos / 3, 1)`, +0.25 si repite semestre | 15 |
| `PERSONAL` | Situación personal/familiar | Casos `PERSONAL` en 90 días | `min(nº casos / 2, 1)` | 5 |
| `UNRESOLVED` | Casos sin resolver | Casos abiertos con antigüedad > 15 días | `min(nº casos_estancados / 2, 1)` | 5 |

Suma de pesos por defecto: **100**. No es obligatorio que sumen 100 — la fórmula normaliza.

## Umbrales de nivel

| Nivel | Rango | Acción institucional sugerida |
|---|---|---|
| `LOW` | 0 – 24 | Ninguna; seguimiento pasivo |
| `MODERATE` | 25 – 49 | Invitación a check-in y a actividades de bienestar |
| `HIGH` | 50 – 74 | Contacto proactivo del área de permanencia en ≤ 5 días hábiles |
| `CRITICAL` | 75 – 100 | Contacto en ≤ 48 h y activación de la ruta de atención |

Los rangos viven en `rsk_models.thresholds` (JSONB) y son editables.

## Reglas de anulación (*override*)

Se aplican **después** de la suma ponderada y pueden elevar el nivel, nunca bajarlo:

| Regla | Condición | Efecto |
|---|---|---|
| OV-1 | Existe un caso `CRITICAL` abierto de categoría psicológica | Nivel mínimo `CRITICAL` |
| OV-2 | El clasificador marcó señal de autolesión o violencia | Nivel mínimo `CRITICAL` + notificación inmediata |
| OV-3 | 3 o más check-ins consecutivos con puntaje ≤ 2 | Nivel mínimo `HIGH` |
| OV-4 | Estudiante con `status = ON_LEAVE` | Nivel mínimo `HIGH` |

Toda anulación aplicada se registra en el desglose con su código, para que la explicación
siga siendo completa.

## Cuándo se recalcula

| Disparador | Momento |
|---|---|
| `CASE_CHANGE` | Al crear, clasificar, cerrar o escalar un caso del estudiante |
| `MOOD_CHECKIN` | Al registrar un check-in |
| `SCHEDULED` | Job diario 02:00 `America/Bogota` para todos los estudiantes activos |
| `MANUAL` | A petición desde el expediente, por quien tenga `risk:read:student` |

El cálculo es **idempotente**: dos ejecuciones con los mismos datos de entrada y el mismo
modelo producen el mismo puntaje.

## Ejemplo trabajado

Estudiante con: 6 sesiones de inasistencia reportadas, 1 caso psicológico no crítico,
media de ánimo 2.4 en 30 días, último check-in hace 10 días, 1 caso económico, sin señal
académica ni personal, 1 caso abierto hace 20 días.

| Factor | `f` | `w` | Puntos |
|---|---|---|---|
| ATTENDANCE | 6/8 = 0.75 | 20 | 15.0 |
| PSYCH_LOAD | 1/3 = 0.33 | 20 | 6.6 |
| MOOD_TREND | (5−2.4)/4 = 0.65 | 15 | 9.8 |
| MOOD_SILENCE | 10/45 = 0.22 | 5 | 1.1 |
| FINANCIAL | 1/2 = 0.50 | 15 | 7.5 |
| ACADEMIC | 0 | 15 | 0.0 |
| PERSONAL | 0 | 5 | 0.0 |
| UNRESOLVED | 1/2 = 0.50 | 5 | 2.5 |
| **Total** | | **100** | **42.5 → `MODERATE`** |

## Advertencias de interpretación

- El puntaje **no es una probabilidad**. Es un índice de priorización de atención. La
  interfaz debe decir «índice de riesgo», nunca «probabilidad de deserción».
- El puntaje depende de que existan reportes: un estudiante sin casos no es
  necesariamente un estudiante sin riesgo. `MOOD_SILENCE` mitiga parcialmente ese sesgo.
- El puntaje no se muestra al docente ni al estudiante ([regla R5](../../01-requisitos/matriz-roles-permisos.md)).
- Cuando exista al menos un período completo con `withdrawn_at` registrado, se debe
  contrastar el puntaje contra el retiro real y recalibrar pesos. Ese contraste es el
  entregable de cierre de la fase 7.
