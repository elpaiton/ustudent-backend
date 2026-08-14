# Modelo entidad-relación (detalle)

Complemento gráfico de [`docs/02-arquitectura/modelo-datos.md`](../../02-arquitectura/modelo-datos.md).

## Identidad y acceso

```mermaid
erDiagram
  IAM_USERS {
    bigserial id PK
    uuid public_id
    citext email UK
    varchar document_number UK
    varchar full_name
    varchar password_hash
    varchar status
    smallint failed_attempts
    timestamptz last_login_at
  }
  IAM_ROLES {
    bigserial id PK
    varchar code UK
    varchar name
    boolean is_system
  }
  IAM_PERMISSIONS {
    bigserial id PK
    varchar code UK
    varchar resource
    varchar action
  }
  IAM_USER_ROLES { bigint user_id FK
    bigint role_id FK }
  IAM_ROLE_PERMISSIONS { bigint role_id FK
    bigint permission_id FK }
  IAM_REFRESH_TOKENS {
    bigserial id PK
    bigint user_id FK
    uuid jti
    uuid family
    boolean revoked
    timestamptz expires_at
  }

  IAM_USERS ||--o{ IAM_USER_ROLES : ""
  IAM_ROLES ||--o{ IAM_USER_ROLES : ""
  IAM_ROLES ||--o{ IAM_ROLE_PERMISSIONS : ""
  IAM_PERMISSIONS ||--o{ IAM_ROLE_PERMISSIONS : ""
  IAM_USERS ||--o{ IAM_REFRESH_TOKENS : ""
```

## Académico

```mermaid
erDiagram
  ACD_PROGRAMS {
    bigserial id PK
    varchar code UK
    varchar name
    varchar faculty
  }
  ACD_STUDENTS {
    bigserial id PK
    bigint user_id FK
    bigint program_id FK
    varchar student_code UK
    varchar admission_period
    smallint current_semester
    varchar status
    timestamptz withdrawn_at
  }
  ACD_TEACHERS {
    bigserial id PK
    bigint user_id FK
    varchar department
  }
  ACD_COURSE_GROUPS {
    bigserial id PK
    bigint teacher_id FK
    bigint program_id FK
    varchar course_code
    varchar group_code
    varchar period
  }
  ACD_ENROLLMENTS {
    bigserial id PK
    bigint student_id FK
    bigint course_group_id FK
    varchar status
  }

  ACD_PROGRAMS ||--o{ ACD_STUDENTS : ""
  ACD_PROGRAMS ||--o{ ACD_COURSE_GROUPS : ""
  ACD_TEACHERS ||--o{ ACD_COURSE_GROUPS : ""
  ACD_COURSE_GROUPS ||--o{ ACD_ENROLLMENTS : ""
  ACD_STUDENTS ||--o{ ACD_ENROLLMENTS : ""
```

## Casos

```mermaid
erDiagram
  CAS_CASES {
    bigserial id PK
    uuid public_id
    varchar case_number UK
    varchar origin
    bigint reporter_user_id FK
    bigint subject_student_id FK
    bigint course_group_id FK
    varchar title
    text description "cifrado"
    varchar category
    varchar priority
    varchar handling_unit
    varchar status
    boolean is_confidential
    bigint assignee_user_id FK
    timestamptz submitted_at
    timestamptz first_response_at
    timestamptz resolved_at
    timestamptz closed_at
    varchar closure_reason
  }
  CAS_CLASSIFICATIONS {
    bigserial id PK
    bigint case_id FK
    varchar suggested_category
    varchar suggested_priority
    varchar suggested_unit
    numeric confidence
    varchar provider
    varchar model_name
    jsonb raw_response
    boolean was_corrected
    bigint reviewed_by_user_id FK
  }
  CAS_FOLLOW_UPS {
    bigserial id PK
    bigint case_id FK
    bigint author_user_id FK
    text note "cifrado"
    boolean visible_to_student
  }
  CAS_ATTACHMENTS {
    bigserial id PK
    bigint case_id FK
    varchar storage_key
    varchar original_name
    varchar content_type
    bigint size_bytes
  }
  CAS_STATUS_HISTORY {
    bigserial id PK
    bigint case_id FK
    varchar from_status
    varchar to_status
    bigint actor_user_id FK
    varchar reason
  }

  CAS_CASES ||--o| CAS_CLASSIFICATIONS : ""
  CAS_CASES ||--o{ CAS_FOLLOW_UPS : ""
  CAS_CASES ||--o{ CAS_ATTACHMENTS : ""
  CAS_CASES ||--o{ CAS_STATUS_HISTORY : ""
```

## Riesgo

```mermaid
erDiagram
  RSK_MODELS {
    bigserial id PK
    varchar version UK
    varchar name
    boolean is_active
    jsonb thresholds
    timestamptz activated_at
  }
  RSK_MODEL_FACTORS {
    bigserial id PK
    bigint model_id FK
    varchar code
    varchar label
    numeric weight
    jsonb params
    boolean enabled
  }
  RSK_ASSESSMENTS {
    bigserial id PK
    bigint student_id FK
    bigint model_id FK
    numeric score
    varchar level
    jsonb factor_breakdown
    varchar trigger
    boolean insufficient_data
    timestamptz computed_at
  }
  RSK_MOOD_CHECKINS {
    bigserial id PK
    bigint student_id FK
    smallint score
    text_array tags
    text comment "cifrado"
    timestamptz created_at
  }

  RSK_MODELS ||--o{ RSK_MODEL_FACTORS : ""
  RSK_MODELS ||--o{ RSK_ASSESSMENTS : ""
```

## Reportes y sistema

```mermaid
erDiagram
  REP_TEMPLATES {
    bigserial id PK
    varchar code
    varchar version
    jsonb sections
    boolean is_active
  }
  REP_REPORTS {
    bigserial id PK
    bigint template_id FK
    varchar period
    jsonb parameters
    jsonb computed_data
    varchar storage_key
    bigint generated_by_user_id FK
    timestamptz generated_at
  }
  SYS_AUDIT_LOG {
    bigserial id PK
    bigint actor_user_id FK
    varchar action
    varchar resource_type
    varchar resource_id
    jsonb metadata
    inet ip_address
    timestamptz occurred_at
  }
  SYS_ASYNC_JOBS {
    bigserial id PK
    varchar type
    jsonb payload
    varchar status
    smallint attempts
    text last_error
    timestamptz run_after
  }

  REP_TEMPLATES ||--o{ REP_REPORTS : ""
```
