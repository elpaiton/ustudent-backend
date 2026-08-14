# Secuencia · Cálculo del índice de riesgo

```mermaid
sequenceDiagram
  autonumber
  participant TR as Disparador<br/>(caso · check-in · job · manual)
  participant RS as RiskAssessmentService
  participant DB as PostgreSQL
  participant IN as RiskInputLoader
  participant EN as RiskEngine (puro)
  participant FA as FactorEvaluators
  participant OV as OverrideRules
  participant NT as Notification

  TR->>RS: evaluar(studentId, trigger)
  RS->>DB: SELECT modelo activo + factores y pesos
  RS->>IN: cargar entradas del estudiante
  IN->>DB: casos 90 días · check-ins 30 días · matrícula · estado
  IN-->>RS: RiskInput (inmutable)

  RS->>EN: calcular(RiskInput, RiskModel)
  loop por cada factor habilitado
    EN->>FA: evaluar(factor, input) → f ∈ [0,1]
    FA-->>EN: valor normalizado
  end
  EN->>EN: score = 100 × Σ(w·f) / Σw
  EN->>OV: aplicar anulaciones
  OV-->>EN: nivel final (solo puede subir)
  EN-->>RS: RiskAssessment { score, level, breakdown, overrides }

  RS->>DB: INSERT rsk_assessments (histórico, nunca UPDATE)

  alt el nivel subió respecto a la última evaluación
    RS->>NT: alerta al coordinador con enlace al expediente
  else sin cambio de nivel
    RS-->>TR: fin silencioso
  end
```

## Job programado diario

```mermaid
sequenceDiagram
  autonumber
  participant SCH as Scheduler 02:00 America/Bogota
  participant RS as RiskAssessmentService
  participant DB as PostgreSQL

  SCH->>DB: SELECT estudiantes ACTIVE (paginado, 500 por lote)
  loop por lote
    SCH->>RS: evaluar(estudiante, SCHEDULED)
    RS->>DB: INSERT rsk_assessments
  end
  SCH->>DB: INSERT sys_audit_log (resumen: procesados, fallidos, duración)
```

Un fallo en un estudiante no aborta el lote: se registra y se continúa. El resumen final
indica cuántos quedaron sin recalcular.

## Invariantes del cálculo

| # | Invariante |
|---|---|
| I1 | El motor es **puro**: mismas entradas y mismo modelo ⇒ mismo resultado. No lee el reloj ni la base de datos. |
| I2 | Toda evaluación guarda la **versión del modelo** usada; las históricas no se recalculan al cambiar los pesos. |
| I3 | El desglose por factor es obligatorio; sin él la evaluación no se persiste. |
| I4 | Las anulaciones solo **elevan** el nivel, nunca lo bajan. |
| I5 | Se inserta siempre una fila nueva: el histórico es inmutable y permite graficar tendencia. |
| I6 | Un estudiante sin datos suficientes obtiene `score = 0` y `level = LOW`, marcado con `insufficientData: true` para no confundir «sin señales» con «sin riesgo». |
