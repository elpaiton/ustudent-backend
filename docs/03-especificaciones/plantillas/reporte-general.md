# Plantilla del reporte general institucional

Define las secciones y variables del informe consolidado que genera la plataforma. La
plantilla es un dato versionado (`rep_templates`), no código: se puede crear una variante
por facultad o por período sin desplegar.

## Parámetros de generación

| Parámetro | Obligatorio | Ejemplo |
|---|---|---|
| `templateId` | sí | `TPL-GENERAL-v1` |
| `period` | sí | `2026-2` |
| `programIds` | no | todos si se omite |
| `dateFrom` / `dateTo` | no | por defecto, el período completo |
| `includeIndividualCases` | no | `false` por defecto — con `true` exige `case:read:any` |

## Estructura

### 1. Portada
Nombre de la institución, título del informe, período, fecha de generación, usuario que lo
generó y versión de la plantilla. Advertencia de confidencialidad al pie.

### 2. Resumen ejecutivo
Párrafo generado a partir de las cifras, con plantilla de texto fija y variables:

> Durante el período **{{period}}** se radicaron **{{totalCases}}** casos correspondientes a
> **{{distinctStudents}}** estudiantes ({{pctOfPopulation}} % de la población activa). El
> **{{pctResolved}} %** fue resuelto, con una mediana de primera respuesta de
> **{{medianFirstResponseHours}} horas**. Al cierre del período,
> **{{studentsHighRisk}}** estudiantes se encuentran en nivel de alerta alto o crítico.

### 3. Volumen y origen
| Métrica | Variable |
|---|---|
| Casos totales | `{{totalCases}}` |
| Autorreportes (estudiante) | `{{casesSelf}}` / `{{pctSelf}}` |
| Reportes de docente | `{{casesStaff}}` / `{{pctStaff}}` |
| Estudiantes distintos atendidos | `{{distinctStudents}}` |
| Casos por 100 estudiantes | `{{casesPer100}}` |

Gráfica: serie mensual de casos por origen.

### 4. Distribución por categoría
Tabla `categoría · nº casos · % · variación frente al período anterior`, con gráfica de
barras horizontales. Se destacan las tres categorías de mayor crecimiento.

### 5. Prioridad y tiempos de atención
| Prioridad | Casos | Mediana 1ª respuesta | % dentro del objetivo |
|---|---|---|---|
| CRITICAL | `{{criticalCount}}` | `{{criticalMedian}}` | `{{criticalSla}}` |
| HIGH | … | … | … |

Objetivos tomados de la [taxonomía](../reglas/taxonomia-clasificacion.md).

### 6. Riesgo de deserción
- Distribución de estudiantes por nivel de alerta, en total y por programa.
- Evolución mensual del promedio del puntaje.
- Los **cinco factores** que más puntos aportaron en el agregado.
- Estudiantes que cambiaron a nivel `HIGH` o `CRITICAL` en el período (conteo; el detalle
  nominal solo aparece con `includeIndividualCases = true`).

### 7. Cobertura de bienestar
Check-ins registrados, participación (% de estudiantes con al menos un check-in al mes),
media de ánimo por programa y su tendencia.

### 8. Desempeño de la clasificación automática
% de aceptación sin corrección, categorías con más correcciones, recall de señales de
urgencia, casos atendidos por el clasificador de respaldo.

### 9. Conclusiones y recomendaciones
Campo de **texto libre**, escrito por la persona responsable. La plataforma no genera
conclusiones automáticas: las cifras las produce el sistema, la lectura la hace el equipo.

### 10. Anexos
Metodología del índice de riesgo (fórmula y pesos del modelo vigente), glosario, versión
del modelo de riesgo y de la plantilla usadas.

## Reglas de generación

1. **Umbral de anonimato**: ningún agregado se publica con menos de 5 estudiantes en la
   celda; se muestra `< 5` en su lugar. Evita reidentificación en programas pequeños.
2. Los datos nominales solo aparecen con `includeIndividualCases = true` y permiso
   `case:read:any`; la generación queda auditada.
3. El informe se persiste con sus cifras congeladas: regenerar el mismo período produce un
   documento nuevo, no modifica el anterior.
4. Todo porcentaje declara su denominador en la nota al pie.
5. El pie de cada página incluye: *«Documento de circulación restringida. Contiene
   información sensible de estudiantes.»*

## Formatos de salida

| Formato | Uso | Implementación |
|---|---|---|
| PDF | Entrega oficial | Plantilla Thymeleaf → HTML → PDF (OpenHTMLtoPDF) |
| HTML | Consulta en línea | Misma plantilla, sin conversión |
| CSV | Datos crudos agregados | Exportación aparte, misma verificación de permisos |
