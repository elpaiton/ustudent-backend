# Estrategia de pruebas

## Pirámide

```
        ╱ E2E ╲            Playwright · 5 recorridos críticos
      ╱─────────╲
    ╱ Integración ╲        Testcontainers · endpoints, alcance por rol, migraciones
  ╱─────────────────╲
╱      Unitarias      ╲    JUnit + Mockito · dominio, motor de riesgo, seudonimizador
```

Cuanto más abajo, más pruebas y más rápidas. Los E2E son pocos y solo cubren recorridos que
no se pueden verificar de otro modo.

## Backend

| Tipo | Herramienta | Qué cubre |
|---|---|---|
| Unitarias de dominio | JUnit 5 | Invariantes de entidades, máquina de estados, validaciones |
| Motor de riesgo | JUnit 5 con `@ParameterizedTest` | **100 %** de las reglas, con tabla de casos por factor |
| Servicios de aplicación | JUnit + Mockito | Orquestación, transacciones, publicación de eventos |
| Repositorios y consultas | `@DataJpaTest` + Testcontainers | Consultas complejas, índices, filtros de alcance |
| API | `@SpringBootTest` + MockMvc + Testcontainers | Contrato, validación, códigos de error |
| Autorización | `@WithMockUser` con permisos concretos | **Un test 403 por endpoint** |
| Arquitectura | ArchUnit | Fronteras de módulo, dirección de dependencias, capas |
| Migraciones | Testcontainers | Flyway aplica limpio desde cero y sobre el esquema anterior |

### Reglas ArchUnit obligatorias

```java
// Ningún módulo accede a la infraestructura de otro
noClasses().that().resideInAPackage("..cases..")
  .should().dependOnClassesThat().resideInAPackage("..risk.infrastructure..");

// El dominio no conoce Spring
noClasses().that().resideInAPackage("..domain..")
  .should().dependOnClassesThat().resideInAPackage("org.springframework..");

// Los controladores no tocan repositorios
noClasses().that().resideInAPackage("..api..")
  .should().dependOnClassesThat().resideInAPackage("..domain.repository..");

// Sin ciclos entre módulos
slices().matching("co.edu.usta.ustudent.(*)..").should().beFreeOfCycles();
```

### Pruebas del motor de riesgo

Tabla de casos por factor, con la entrada, el valor normalizado esperado y los puntos:

| Caso | Entrada | `f` esperado | Nota |
|---|---|---|---|
| Sin inasistencia | 0 sesiones | 0.00 | Límite inferior |
| Inasistencia parcial | 6 de 8 | 0.75 | Caso del ejemplo trabajado |
| Inasistencia saturada | 20 sesiones | 1.00 | El `min` acota |
| Ánimo neutro | media 3.0 | 0.50 | Punto medio |
| Ánimo óptimo | media 5.0 | 0.00 | Límite |
| Sin check-ins | ninguno | — | Marca `insufficientData` |

Además: un test de **propiedad** que verifica que el puntaje siempre queda en [0, 100] con
cualquier combinación de entradas, y un test que confirma que dos ejecuciones idénticas dan
el mismo resultado.

## Frontend

| Tipo | Herramienta | Qué cubre |
|---|---|---|
| Unitarias | Vitest | Utilidades, esquemas Zod, formateadores |
| Componentes | Testing Library | Estados de carga/error/vacío, accesibilidad básica |
| Contrato | Tipos generados de OpenAPI | El build falla si el backend cambia el contrato |
| E2E | Playwright | Recorridos críticos, con datos sembrados |

### Recorridos E2E (los únicos cinco)

1. Estudiante inicia sesión → radica solicitud → ve su radicado.
2. Docente inicia sesión → busca estudiante de su grupo → reporta inasistencia.
3. Profesional atiende: bandeja → detalle → asignar → seguimiento → resolver → cerrar.
4. Coordinador consulta el tablero de riesgo y abre el desglose de un estudiante.
5. Administrador crea un rol, le asigna permisos y verifica que el menú del usuario cambia.

## Pruebas de seguridad

| Prueba | Frecuencia |
|---|---|
| 403 por endpoint con rol insuficiente | Cada PR |
| Alcance por fila: un docente no ve estudiantes ajenos | Cada PR |
| El puntaje de riesgo no aparece en ninguna respuesta dirigida a docente o estudiante | Cada PR |
| El seudonimizador no deja pasar documentos ni correos | Cada PR |
| OWASP Dependency-Check | Cada PR |
| Revisión ASVS nivel 1 | Cada entrega mayor |

## Pruebas de la integración con IA

- **Nunca** se llama al proveedor real en las pruebas automáticas: se usa un adaptador falso.
- Pruebas de degradación: timeout, error 500, JSON inválido, categoría fuera de taxonomía.
  Todas deben terminar con el caso en `PENDING_REVIEW` y clasificado por reglas.
- Conjunto de 40 casos etiquetados a mano para medir precisión; se ejecuta manualmente al
  cambiar el prompt, no en CI (tiene costo y latencia).

## Umbrales que rompen el build

| Métrica | Umbral |
|---|---|
| Cobertura global del backend | ≥ 70 % |
| Cobertura del paquete `risk` | 100 % |
| Cobertura del paquete `iam` | ≥ 85 % |
| Violaciones de ArchUnit | 0 |
| Vulnerabilidades críticas de dependencias | 0 |
| Pares de color por debajo del contraste mínimo | 0 |

## Datos de prueba

`tools/scripts/seed-demo` genera un conjunto sintético coherente: 200 estudiantes, 20
docentes, 5 programas y ~400 casos repartidos por categoría y a lo largo del tiempo, de modo
que el tablero de riesgo tenga forma realista. **Ningún dato real, nunca**, ni siquiera
parcialmente.
