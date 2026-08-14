# Secuencia · Radicación y clasificación de un caso

```mermaid
sequenceDiagram
  autonumber
  actor U as Estudiante / Docente
  participant FE as Next.js
  participant API as CaseController
  participant CS as CaseService
  participant DB as PostgreSQL
  participant JOB as JobRunner
  participant CL as ClassificationService
  participant PS as Pseudonymizer
  participant LLM as Proveedor LLM
  participant RE as RiskEngine
  participant NT as Notification

  U->>FE: completa el formulario
  FE->>API: POST /cases (Idempotency-Key)
  API->>CS: crear(comando)
  CS->>DB: INSERT cas_cases (status SUBMITTED)
  CS->>DB: INSERT sys_async_jobs (CLASSIFY_CASE)
  CS-->>API: caso creado
  API-->>FE: 201 { caseNumber, classificationStatus: PENDING }
  FE-->>U: «Radicado US-2026-000123»

  Note over CS,JOB: después del commit (AFTER_COMMIT)

  JOB->>CL: clasificar(caseId)
  CL->>DB: SELECT título y descripción
  CL->>PS: seudonimizar(texto)
  PS-->>CL: texto sin identificadores
  CL->>LLM: POST clasificación (timeout 5 s)

  alt respuesta válida y confianza ≥ 0.55
    LLM-->>CL: { category, priority, unit, confidence }
    CL->>DB: INSERT cas_classifications (provider LLM)
    CL->>DB: UPDATE cas_cases → CLASSIFIED
  else error, timeout o respuesta inválida
    LLM--x CL: fallo
    CL->>CL: clasificador de reglas (léxico)
    CL->>DB: INSERT cas_classifications (provider RULES)
    CL->>DB: UPDATE cas_cases → PENDING_REVIEW
  end

  opt señal de urgencia crítica detectada
    CL->>DB: UPDATE prioridad = CRITICAL, área = PSYCHOLOGY
    CL->>NT: alerta inmediata al área
    NT-->>FE: aviso in-app + correo
  end

  CL->>RE: recalcular riesgo (trigger CASE_CHANGE)
  RE->>DB: INSERT rsk_assessments (score, level, breakdown)

  opt el nivel sube a HIGH o CRITICAL
    RE->>NT: alerta al coordinador de permanencia
  end

  NT-->>U: confirmación de radicación por correo
```

## Puntos de diseño

| # | Decisión | Motivo |
|---|---|---|
| 1 | La respuesta 201 no espera a la IA | Cumple el [RNF-P2](../../01-requisitos/requisitos-no-funcionales.md): radicar responde en < 1 s |
| 2 | El trabajo se encola **en la misma transacción** que el caso | Si el caso se persiste, su clasificación está garantizada; si falla, no queda trabajo huérfano |
| 3 | `Idempotency-Key` en el POST | Un doble clic o un reintento de red no crea dos radicados |
| 4 | La detección de urgencia corre con ambos clasificadores | La red de seguridad no puede depender de un proveedor externo |
| 5 | El recálculo de riesgo ocurre tras clasificar, no al radicar | La categoría es entrada de varios factores; hacerlo antes daría un puntaje incompleto |
| 6 | La seudonimización ocurre dentro del servicio, no en el adaptador | Ningún adaptador futuro puede saltársela por olvido |
