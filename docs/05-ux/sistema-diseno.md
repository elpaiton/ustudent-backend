# Sistema de diseño

Interfaz **moderna, minimalista y académica**: mucho aire, poca decoración, jerarquía clara y
color usado con intención. El azul aporta la seriedad institucional; el turquesa es el color
de acción y de dato vivo. Nada más compite por la atención.

## Principios

1. **El contenido manda.** Fondos neutros, sin sombras pesadas ni degradados decorativos. El
   color aparece donde hay una acción o un dato que leer.
2. **Un solo acento por pantalla.** Si todo resalta, nada resalta.
3. **El estado siempre visible.** Cada caso, cada solicitud y cada alerta muestran su estado
   con la misma pastilla en todas partes.
4. **Sensible por diseño.** La interfaz nunca muestra un puntaje de riesgo a quien no debe
   verlo, y nunca describe a un estudiante con etiquetas clínicas.
5. **Tono humano.** «Cuéntanos qué está pasando», no «Ingrese la descripción del incidente».

## Paleta

### Azul institucional — color de marca y navegación

| Token | Hex | Uso |
|---|---|---|
| `blue-50` | `#EEF4FB` | Fondos de sección, filas alternas |
| `blue-100` | `#D6E4F5` | Bordes suaves, estados hover claros |
| `blue-200` | `#AEC9EA` | Separadores destacados |
| `blue-400` | `#4A87CC` | Iconografía secundaria |
| `blue-600` | `#1B5FA8` | **Color primario**: botones, enlaces, foco |
| `blue-700` | `#154B85` | Hover del primario |
| `blue-900` | `#0A2540` | Encabezados, barra lateral, texto de máxima jerarquía |

### Turquesa — acento, datos y confirmación

| Token | Hex | Uso |
|---|---|---|
| `teal-50` | `#E6F7F7` | Fondo de tarjetas de métrica |
| `teal-300` | `#5FD0CE` | Series secundarias en gráficas |
| `teal-500` | `#14A5A0` | **Acento**: métricas, indicadores, gráficas |
| `teal-700` | `#0C7A76` | Texto turquesa sobre fondo claro (garantiza contraste) |

> Regla: `teal-500` nunca se usa como color de texto sobre blanco; para texto se usa
> `teal-700`. Como relleno de gráfica o de indicador, `teal-500` es el correcto.

### Neutros

| Token | Hex | Uso |
|---|---|---|
| `neutral-0` | `#FFFFFF` | Superficie de tarjetas |
| `neutral-50` | `#F7F9FC` | Fondo de la aplicación |
| `neutral-200` | `#E3E8EF` | Bordes y divisores |
| `neutral-500` | `#697586` | Texto secundario |
| `neutral-700` | `#3F4A5A` | Texto de cuerpo |
| `neutral-900` | `#121926` | Títulos |

### Semánticos

| Token | Hex | Significado |
|---|---|---|
| `success` | `#0E8A5F` | Resuelto, confirmado |
| `warning` | `#B26A00` | Requiere atención, pendiente de revisión |
| `danger` | `#C0362C` | Crítico, error |
| `info` | `#1B5FA8` | Informativo (coincide con el primario) |

### Niveles de riesgo

Escala propia, deliberadamente **no** de verde a rojo puro: el objetivo es priorizar
atención, no señalar culpables.

| Nivel | Fondo | Texto | Hex fondo |
|---|---|---|---|
| `LOW` | teal muy claro | teal oscuro | `#E6F7F7` / `#0C7A76` |
| `MODERATE` | ámbar claro | ámbar oscuro | `#FDF3E2` / `#8A5300` |
| `HIGH` | naranja claro | naranja oscuro | `#FCEBDF` / `#A64B12` |
| `CRITICAL` | rojo claro | rojo oscuro | `#FBE9E7` / `#98271E` |

El nivel **nunca** se comunica solo por color: siempre lleva etiqueta de texto (RNF-U1).

## Tipografía

| Rol | Familia | Tamaño / interlineado | Peso |
|---|---|---|---|
| Display | Inter | 32 / 40 | 600 |
| H1 | Inter | 24 / 32 | 600 |
| H2 | Inter | 20 / 28 | 600 |
| H3 | Inter | 16 / 24 | 600 |
| Cuerpo | Inter | 15 / 24 | 400 |
| Secundario | Inter | 13 / 20 | 400 |
| Dato numérico | Inter Tight (tabular) | 28 / 32 | 600 |

Una sola familia. Los números de métricas usan cifras tabulares para que no bailen al
actualizarse.

## Espaciado, radios y elevación

- Escala de 4 px: `4 · 8 · 12 · 16 · 24 · 32 · 48 · 64`.
- Radios: `6 px` en controles, `12 px` en tarjetas, `999 px` en pastillas.
- Elevación: solo dos niveles. `sm` = `0 1px 2px rgba(16,24,40,.06)` para tarjetas;
  `md` = `0 8px 24px rgba(16,24,40,.10)` para modales y menús. Nada más.
- Ancho máximo de contenido: `1280 px`; longitud de línea de texto largo ≤ `72ch`.

## Tokens

`apps/frontend/src/styles/tokens.css`:

```css
:root {
  --color-primary: #1B5FA8;
  --color-primary-hover: #154B85;
  --color-accent: #14A5A0;
  --color-accent-text: #0C7A76;
  --color-surface: #FFFFFF;
  --color-background: #F7F9FC;
  --color-border: #E3E8EF;
  --color-text: #3F4A5A;
  --color-text-strong: #121926;
  --color-text-muted: #697586;
  --radius-control: 6px;
  --radius-card: 12px;
  --shadow-sm: 0 1px 2px rgba(16, 24, 40, .06);
  --shadow-md: 0 8px 24px rgba(16, 24, 40, .10);
}
```

Tailwind 4 se configura desde CSS, no desde `tailwind.config.ts`: los tokens se declaran en
un bloque `@theme` dentro de `tokens.css` y Tailwind genera a partir de ellos las utilidades
(`bg-blue-600`, `text-teal-700`, …). Existe por tanto **una sola** definición de cada color, y
sigue siendo accesible como `var(--color-*)` para los casos que no cubre una utilidad.

## Componentes base (`src/components/ui/`)

`Button` (primary · secondary · ghost · danger) · `Input` · `Textarea` · `Select` ·
`Checkbox` · `RadioGroup` · `Card` · `Badge` · `StatusPill` · `RiskBadge` · `Table` ·
`Pagination` · `Tabs` · `Modal` · `Drawer` · `Toast` · `EmptyState` · `Skeleton` ·
`Avatar` · `Timeline` · `FileUpload` · `MoodScale`.

Reglas transversales:
- Todo control tiene estado de foco visible (`outline: 2px solid var(--color-primary)`, `offset: 2px`).
- Todo listado define su `EmptyState` con texto útil, no «Sin datos».
- Toda carga usa `Skeleton` con la forma del contenido, no un *spinner* centrado.
- Todo formulario muestra los errores junto al campo y resume al inicio si son varios.

## Componentes de dominio

| Componente | Descripción |
|---|---|
| `CaseCard` | Radicado, título, categoría, prioridad, estado, antigüedad |
| `CaseTimeline` | Línea de tiempo de transiciones y seguimientos |
| `MoodScale` | Escala 1–5 con caras e etiqueta textual; táctil y accesible por teclado |
| `RiskBreakdown` | Barra por factor con su aporte en puntos — **nunca se muestra el puntaje sin ella** |
| `RiskDistributionChart` | Barras apiladas por nivel y programa |
| `ClassificationSuggestion` | Sugerencia de la IA con confianza y botones aceptar / corregir |
| `StudentSearch` | Buscador con alcance limitado a los grupos del docente |

## Accesibilidad

- Contraste mínimo 4.5:1 en texto y 3:1 en elementos de interfaz; se verifica con un script
  en CI sobre los pares de tokens aprobados.
- Navegación completa por teclado, con orden de tabulación lógico y salto al contenido.
- Modales con foco atrapado y cierre con `Esc`.
- Estado nunca comunicado solo por color: siempre color + texto (+ icono cuando ayude).
- Etiquetas reales asociadas a cada campo; los `placeholder` no sustituyen etiquetas.
- Movimiento respetuoso con `prefers-reduced-motion`.

## Tono de la interfaz

| En vez de | Escribir |
|---|---|
| «Ingrese la descripción del incidente» | «Cuéntanos qué está pasando» |
| «Su caso ha sido rechazado» | «Cerramos este caso porque corresponde a otra dependencia. Te contamos a dónde acudir.» |
| «Estudiante en riesgo alto» | «Requiere contacto prioritario» |
| «Sin datos» | «Aún no has radicado ninguna solicitud» |
