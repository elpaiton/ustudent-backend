# Taxonomía de clasificación

Lista **cerrada**. El clasificador no puede devolver valores fuera de estas tablas; si lo
hace, la respuesta se descarta y actúa el clasificador de respaldo.

## Categorías

| Código | Nombre | Ejemplos | Área por defecto | Prioridad base |
|---|---|---|---|---|
| `ACADEMIC` | Académica | Bajo rendimiento, pérdida de asignatura, dificultad con una materia | `ACADEMIC_ADVISING` | `MEDIUM` |
| `ATTENDANCE` | Inasistencia | Faltas recurrentes, abandono de clases | `ACADEMIC_ADVISING` | `MEDIUM` |
| `PSYCHOLOGICAL` | Psicológica / emocional | Ansiedad, depresión, estrés, duelo, aislamiento | `PSYCHOLOGY` | `HIGH` |
| `FINANCIAL` | Económica | Dificultad de pago, pérdida de beca, gastos de transporte | `FINANCIAL_AID` | `HIGH` |
| `PERSONAL` | Personal / familiar | Cambio de domicilio, cuidado de familiares, conflicto familiar | `WELLBEING` | `MEDIUM` |
| `PHYSICAL_HEALTH` | Salud física | Enfermedad, incapacidad, accidente | `WELLBEING` | `HIGH` |
| `COEXISTENCE` | Convivencia | Acoso, discriminación, conflicto entre pares | `WELLBEING` | `HIGH` |
| `ADMINISTRATIVE` | Administrativa | Matrícula, certificados, trámites, horarios | `REGISTRAR` | `LOW` |
| `OTHER` | Otra | No encaja en las anteriores | `WELLBEING` | `LOW` |

## Prioridades

| Código | Significado | Tiempo objetivo de primera respuesta |
|---|---|---|
| `LOW` | Trámite o consulta sin urgencia | 5 días hábiles |
| `MEDIUM` | Afecta el desempeño, sin riesgo inmediato | 3 días hábiles |
| `HIGH` | Riesgo de abandono o afectación seria | 24 horas |
| `CRITICAL` | Riesgo para la integridad de la persona | 4 horas, con notificación inmediata |

## Áreas responsables

| Código | Área |
|---|---|
| `WELLBEING` | Bienestar universitario |
| `PSYCHOLOGY` | Departamento de psicología |
| `ACADEMIC_ADVISING` | Consejería académica |
| `FINANCIAL_AID` | Apoyo financiero y becas |
| `REGISTRAR` | Registro y control académico |

## Señales de urgencia

Se evalúan **siempre**, con cualquiera de los dos clasificadores, como red de seguridad
independiente del LLM.

| Código | Descripción | Efecto |
|---|---|---|
| `SELF_HARM_MENTION` | Menciones de autolesión o ideación suicida | `CRITICAL` + `PSYCHOLOGY` + notificación inmediata + regla OV-2 del motor de riesgo |
| `VIOLENCE_MENTION` | Violencia sufrida o ejercida, acoso grave | `CRITICAL` + `WELLBEING` + notificación inmediata |
| `DROPOUT_INTENT` | Manifiesta intención de retirarse | Prioridad mínima `HIGH` |
| `ECONOMIC_EMERGENCY` | Riesgo inminente de perder el semestre por dinero | Prioridad mínima `HIGH` |

Ante `SELF_HARM_MENTION` o `VIOLENCE_MENTION`, la interfaz muestra al usuario, en el acto,
las líneas de atención institucionales y nacionales. Ese texto es contenido configurable, no
generado por IA.

## Reglas de derivación de prioridad

Se aplican en orden; la primera que coincide manda:

1. Hay señal de urgencia crítica ⇒ `CRITICAL`.
2. El caso es reporte de docente con inasistencia ≥ 6 sesiones ⇒ mínimo `HIGH`.
3. El estudiante ya tiene un caso abierto de la misma categoría ⇒ sube un nivel.
4. En cualquier otro caso ⇒ prioridad base de la categoría.

## Medición

Se registran la sugerencia y la decisión humana. Métricas del tablero
([US-E4-4](../../01-requisitos/historias-usuario.md)):

- **Aceptación**: % de casos donde la categoría final = categoría sugerida.
- **Matriz de confusión** por categoría, para detectar cuáles se confunden entre sí.
- **Recall de urgencia**: de los casos que resultaron críticos, cuántos fueron detectados
  automáticamente. Esta es la métrica que no puede degradarse; se revisa cada mes.
