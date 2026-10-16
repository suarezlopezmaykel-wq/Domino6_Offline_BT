# INFORME TÉCNICO DE IMPLEMENTACIÓN Y ANÁLISIS GEOMÉTRICO DEFECTO DE GIROS - DOMINO6 OFFLINE BT

**Fecha:** 15 de Octubre de 2026
**Investigador / Agente:** Jules
**Rama de trabajo:** `jules-9846497619956500543-ba9cd156`
**Repositorio:** `https://github.com/suarezlopezmaykel-wq/Domino6_Offline_BT`

---

## 1. DIAGNÓSTICO DEL DEFECTO Y CAUSA GEOMÉTRICA RAÍZ

### Evidencia de la prueba real y causa matemática:
1. En ejecuciones anteriores, se sumaba/restaba una distancia adicional `verticalW` (34 px) sobre $xEdge$ dentro del bloque de giro `else`.
2. Como la última ficha horizontal dibujada que sí cupo en pantalla ocupaba el intervalo $[xEdge - 52, xEdge]$ (para `rowDir > 0`), el borde libre exterior de la cadena es $xEdge$.
3. Al modificar $xEdge$ restándole $34$ px (`xEdge = xEdge - verticalW`), el valor de $xEdge$ retrocedía artificialmente a $xEdge - 34$.
4. Cuando el bucle invertía el sentido de avance (`rowDir = -rowDir`), la primera ficha horizontal de la nueva fila colocaba su borde exterior derecho en el $xEdge$ desfasado ($xEdge - 34$), provocando un desplazamiento lateral de $34$ píxeles. Esto originaba un desempalme visual entre la columna de giros y la nueva fila horizontal.

---

## 2. SOLUCIÓN GEOMÉTRICA DE CONTIGÜIDAD APLICADA

### Código exacto implementado en `BoardView.drawBranch()` (`MainActivity.java`):
```java
float colX = rowDir > 0 ? xEdge - verticalW / 2f : xEdge + verticalW / 2f;
int vertCount = 0;
for (int k = 0; k < 2 && index >= 0 && index < g.board.size(); k++) {
    D v = g.board.get(index);
    int vn = step > 0 ? v.a : v.z, vf = step > 0 ? v.z : v.a;
    float yc = y + vs * (normalH / 2f + verticalH / 2f + k * verticalH);
    if (vs < 0) drawVertical(c, vf, vn, colX, yc, true);
    else drawVertical(c, vn, vf, colX, yc, true);
    index += step;
    vertCount++;
}
y = y + vs * (normalH + vertCount * verticalH);
rowDir = -rowDir;
placedInRow = false;
```

### Principios de la solución:
- **Centrado de la columna de giro (`colX`):** Para `rowDir > 0`, `colX = xEdge - verticalW / 2f` coloca las fichas verticales abarcando el intervalo $[xEdge - 34, xEdge]$, quedando perfectamente alineadas con el extremo derecho libre de la fila anterior.
- **Sin desfasaje en $xEdge$:** Se eliminó la reasignación de $xEdge$. La nueva fila horizontal utiliza $xEdge$ como su límite exterior contiguo, iniciando su trazado exactamente en tangente a la columna vertical sin huecos ni saltos laterales.
- **Salto Vertical exacto ($Y$):** `y = y + vs * (normalH + vertCount * verticalH)` calcula la distancia $Y$ acumulada exacta según las `vertCount` fichas verticales realmente colocadas (evitando colisiones verticales en giros sucesivos).

---

## 3. ADAPTACIÓN RESPONSIVE A PANTALLAS (SAMSUNG GALAXY A03 CORE & TABLET ANDROID 13)

- **Escalado del tablero:** `int side = Math.max(440, Math.min(getResources().getDisplayMetrics().widthPixels, 560));` escala el tablero en píxeles de pantalla proporcionalmente.
- **Mano del jugador:** `HorizontalScrollView` con `setFillViewport(true)` permite visualización fluida sin solapamientos en dispositivos de 720p (como Samsung Galaxy A03 Core) y pantallas de mayor resolución de tablets Android 13.
- **Declaración sobre pruebas visuales:** En el entorno sandbox no contamos con un emulador Android gráfico GUI o dispositivos físicos conectados. La prueba de compilación en GitHub Actions garantiza que el código fuente compila limpiamente, pero la verificación visual interactiva debe realizarse instalando el APK generado en el dispositivo.

---

## 4. ARCHIVOS MODIFICADOS Y SYNCRONIZACIÓN

1. `app/src/main/java/com/example/domino6/MainActivity.java`
2. `Domino6_V15.zip`
3. `.github/workflows/compilar.yml`
4. `INFORME_JULES.md`

---

## 5. ENLACES DIRECTOS

- **Informe en GitHub:** [INFORME_JULES.md](https://github.com/suarezlopezmaykel-wq/Domino6_Offline_BT/blob/jules-9846497619956500543-ba9cd156/INFORME_JULES.md)
- **Acceso a GitHub Actions:** [Pestaña Actions en el Repositorio](https://github.com/suarezlopezmaykel-wq/Domino6_Offline_BT/actions)
- **Confirmación de NO FUSIÓN:** La rama no ha sido fusionada hacia `main`.
