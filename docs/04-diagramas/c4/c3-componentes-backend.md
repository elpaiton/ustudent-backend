# C4 · Nivel 3 — Componentes del backend

Detalle interno del contenedor «API monolítica». Cada bloque es un módulo con sus capas.

```mermaid
flowchart TB
  subgraph WEB["Capa web"]
    SEC["SecurityFilterChain<br/>JWT · CSRF · CORS"]
    ERR["GlobalExceptionHandler<br/>RFC 7807"]
    CTRL["Controladores REST"]
  end

  subgraph CASES["Módulo cases"]
    CS["CaseService<br/>radicación · transiciones"]
    CAS_A["CaseAssignmentService"]
    CFU["FollowUpService"]
    CREPO[("CaseRepository")]
  end

  subgraph AI["Módulo ai"]
    CLS["ClassificationService"]
    PORT{{"CaseClassifierPort"}}
    LLMA["LlmClassifierAdapter"]
    RULA["RuleBasedClassifierAdapter"]
    PSE["Pseudonymizer"]
  end

  subgraph RISK["Módulo risk"]
    RE["RiskEngine"]
    FAC["FactorEvaluators<br/>(uno por factor)"]
    OVR["OverrideRules"]
    RREPO[("AssessmentRepository")]
  end

  subgraph IAM["Módulo iam"]
    AUTH["AuthService"]
    USR["UserService"]
    ROL["RoleService"]
    PERM["PermissionEvaluator"]
  end

  subgraph ACA["Módulo academic"]
    STU["StudentService"]
    GRP["CourseGroupService"]
    IMP["StudentImportService"]
  end

  subgraph REP["Módulo reporting"]
    RPT["ReportService"]
    TPL["TemplateEngine<br/>Thymeleaf → PDF"]
  end

  NOT["Módulo notification<br/>correo + avisos in-app"]
  AUD["shared/audit<br/>AuditAspect"]
  JOBS["shared · JobRunner<br/>sys_async_jobs"]

  CTRL --> CS & USR & ROL & STU & RPT
  SEC --> PERM
  CS --> CREPO
  CS -->|CaseCreated| JOBS --> CLS
  CLS --> PSE --> PORT
  PORT --> LLMA
  PORT --> RULA
  LLMA -.fallback.-> RULA
  CLS -->|CaseClassified| CS
  CS -->|CaseChanged| RE
  RE --> FAC --> RREPO
  RE --> OVR
  CS --> ACA
  RE --> ACA
  RPT --> RE & CS
  RPT --> TPL
  CS & RE --> NOT
  CS & USR & ROL & RE --> AUD
```

## Componentes clave

| Componente | Responsabilidad | Nota de diseño |
|---|---|---|
| `CaseService` | Radicar, transicionar y cerrar casos; publica eventos de dominio | Único punto donde se abre transacción para casos |
| `PermissionEvaluator` | Resuelve `hasAuthority('recurso:acción')` desde el token | Se integra con `@PreAuthorize`; no consulta la BD en cada petición |
| `ClassificationService` | Orquesta seudonimización, llamada al puerto, validación y persistencia | No conoce al proveedor concreto |
| `Pseudonymizer` | Elimina identificadores antes de salir del sistema | Se prueba con casos límite: nombres compuestos, documentos con puntos |
| `RiskEngine` | Suma ponderada, aplica anulaciones, persiste el desglose | Puro y determinista; sin acceso a BD, recibe un `RiskInput` ya cargado |
| `FactorEvaluators` | Un evaluador por factor, todos con la misma interfaz | Agregar un factor = agregar una clase + una fila de configuración |
| `AuditAspect` | Intercepta `@Audited` y escribe la bitácora | Fuera del hilo de la transacción de negocio |
| `JobRunner` | Toma trabajos pendientes, reintenta con retroceso exponencial | Sustituible por un broker sin tocar el dominio |

## Por qué `RiskEngine` es puro

Recibe un objeto de entrada ya materializado (casos, check-ins, matrícula) y devuelve la
evaluación. Al no tocar la base de datos ni el reloj del sistema, se prueba exhaustivamente
con tablas de casos, que es exactamente lo que exige el
[RNF-M1](../../01-requisitos/requisitos-no-funcionales.md): 100 % de cobertura de las reglas
de riesgo.
