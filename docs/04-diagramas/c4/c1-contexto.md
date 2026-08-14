# C4 · Nivel 1 — Contexto del sistema

```mermaid
flowchart TB
  EST(["Estudiante"])
  DOC(["Docente"])
  PRO(["Profesional de bienestar"])
  COO(["Coordinador de permanencia"])
  ADM(["Administrador"])

  SYS["<b>uStudent</b><br/>Plataforma de promoción<br/>y permanencia estudiantil"]

  SIA["Sistema académico<br/>institucional"]
  LLM["Proveedor de LLM"]
  SMTP["Servidor de correo"]

  EST -->|"radica solicitudes,<br/>registra su estado de ánimo"| SYS
  DOC -->|"reporta casos de<br/>sus estudiantes"| SYS
  PRO -->|"atiende casos,<br/>registra seguimientos"| SYS
  COO -->|"consulta riesgo,<br/>genera informes"| SYS
  ADM -->|"gestiona usuarios, roles,<br/>permisos y parámetros"| SYS

  SIA -.->|"carga CSV de estudiantes,<br/>programas y grupos"| SYS
  SYS -->|"clasifica texto<br/>seudonimizado"| LLM
  SYS -->|"notificaciones"| SMTP
  SYS -->|"correos"| EST
```

## Alcance del sistema

uStudent es el **sistema de registro** de los casos de bienestar y del índice de riesgo. No
es sistema de registro de la información académica: esa se importa y se trata como
referencia de solo lectura.

## Dependencias externas

| Sistema | Naturaleza | Qué pasa si falla |
|---|---|---|
| Sistema académico | Importación manual por CSV (v1) | Se opera con el último corte cargado |
| Proveedor de LLM | Síncrono, con timeout de 5 s | Actúa el clasificador de respaldo; el servicio sigue |
| Servidor SMTP | Asíncrono con reintento | Los avisos in-app siguen funcionando; los correos se reintentan |
