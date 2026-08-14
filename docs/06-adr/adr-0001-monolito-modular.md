# ADR-0001 · Backend como monolito modular

- **Estado**: aceptado
- **Fecha**: 2026-08-13
- **Decide**: equipo de desarrollo

## Contexto

Hay que elegir la topología del backend. El sistema tiene siete áreas funcionales
razonablemente separables (identidad, académico, casos, IA, riesgo, reportes,
notificaciones) y un equipo pequeño sin plataforma de orquestación propia. El volumen
esperado es de 20 000 estudiantes y ~40 000 casos al año: bajo para estándares de
microservicios.

## Decisión

Un **monolito modular** desplegable como un solo artefacto, con módulos internos de
frontera explícita:

- Cada módulo tiene su paquete raíz y sus capas `api / application / domain / infrastructure`.
- Ningún módulo consulta las tablas de otro; se comunican por interfaz de aplicación o por
  evento de dominio.
- El grafo de dependencias entre módulos es acíclico.
- Las reglas se verifican en CI con **ArchUnit**; violarlas rompe el build.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| Microservicios desde el inicio | Costo operativo (despliegue, trazas, consistencia distribuida) desproporcionado para el volumen y el tamaño del equipo. Aún no conocemos bien las fronteras del dominio. |
| Monolito sin módulos (por capas) | Con siete áreas y datos sensibles, una capa `services` común se vuelve inmanejable y difumina las reglas de acceso. |
| Monolito modular con módulos Maven separados | Aporta garantía de compilación, pero encarece el arranque del proyecto. Se deja como paso posterior si ArchUnit resulta insuficiente. |

## Consecuencias

**Positivas**
- Una transacción de base de datos cubre operaciones que en microservicios exigirían saga.
- Depuración y pruebas de integración locales sencillas (un proceso, un Testcontainer).
- Un solo despliegue, un solo pipeline.

**Negativas**
- El aislamiento depende de disciplina y de ArchUnit, no del compilador.
- No se puede escalar un módulo por separado; se escala todo el artefacto.
- Un fallo grave (fuga de memoria) afecta a todas las funciones.

**Mitigaciones**
- Pruebas de arquitectura obligatorias en CI.
- Eventos de dominio en los puntos donde una extracción futura sería más probable
  (`ai`, `notification`), que ya hoy se comunican de forma asíncrona.

## Cuándo revisar

Si aparece alguna de estas señales: el tiempo de build supera los 10 minutos, un módulo
necesita perfil de escalado radicalmente distinto, o dos equipos independientes empiezan a
pisarse en el mismo repositorio.
