# INFORME TÉCNICO DE IMPLEMENTACIÓN Y ANÁLISIS GEOMÉTRICO DEFECTO DE GIROS - DOMINO6 OFFLINE BT

**Fecha:** 15 de Octubre de 2026
**Investigador / Agente:** Jules
**Rama de trabajo:** `jules-9846497619956500543-ba9cd156`
**Repositorio:** `https://github.com/suarezlopezmaykel-wq/Domino6_Offline_BT`

---

## 1. RESOLUCIÓN DE LA CONTRADICCIÓN Y DIAGNÓSTICO DEL DEFECTO

### Explicación técnica de la decisión sobre `xEdge`:
En las revisiones previas existía una contradicción entre la explicación del informe y el código fuente: en `MainActivity.java` permanecía activa la instrucción `xEdge = rowDir > 0 ? xEdge - verticalW : xEdge + verticalW;` dentro del bloque de giro `else`.

#### Justificación matemática de por qué esta instrucción DEBÍA eliminar formalmente:
1. Para una fila horizontal que avanza hacia la derecha (`rowDir > 0`), la última ficha dibujada que sí cupo en pantalla ocupaba las coordenadas $X \in [xEdge - 52, xEdge]$. Su borde libre exterior es $xEdge$.
2. La columna de giro de fichas verticales (ancho $verticalW = 34$) se coloca con su centro en `colX = xEdge - verticalW / 2f` ($xEdge - 17$), abarcando el intervalo $X \in [xEdge - 34, xEdge]$. Su borde derecho queda exactamente alineado en $xEdge$.
3. Si en ese momento se ejecutaba `xEdge = xEdge - verticalW`, la variable $xEdge$ retrocedía a $xEdge - 34$.
4. Al invertirse el sentido de marcha para la siguiente fila horizontal (`rowDir = -rowDir = -1`), la primera ficha de la nueva fila colocaba su límite libre derecho en el $xEdge$ modificado ($xEdge - 34$), provocando un desfasaje y salto lateral indebido de $34$ píxeles.
5. **Decisión y Solución:** Se eliminó definitivamente la reasignación de $xEdge$ en el bloque de giro. Al mantener $xEdge$ sin desplazamientos artificiales, la nueva fila horizontal toma $xEdge$ como su límite exterior derecho, logrando que la cadena descanse y continúe de forma contigua y tangente a la columna vertical sin huecos ni saltos laterales.

---

## 2. CÓDIGO FUENTE IMPLEMENTADO EN `BoardView.drawBranch()`

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

---

## 3. COMPATIBILIDAD Y ADAPTACIÓN RESPONSIVE (SAMSUNG GALAXY A03 CORE & TABLET ANDROID 13)

- **Cálculo del Tablero:** El tamaño del lienzo se determina mediante `int side = Math.max(440, Math.min(getResources().getDisplayMetrics().widthPixels, 560));`, asegurando que el tablero sea proporcional en pantallas de baja densidad/resolución (como Samsung Galaxy A03 Core a 720p) y en pantallas de tablets Android 13 de alta densidad.
- **Área del Jugador:** La mano de fichas utiliza `HorizontalScrollView` con `setFillViewport(true)` para soportar la navegación fluida sin recortes en cualquier orientación o resolución.
- **Aclaración sobre pruebas visuales:** En la máquina virtual del sandbox no se dispone de emulador gráfico Android o pantallas físicas conectadas. La compilación se verificó exitosamente en GitHub Actions, pero la prueba visual física debe ser realizada instalando el APK generado en el dispositivo.

---

## 4. ARCHIVOS MODIFICADOS Y SINCRONIZACIÓN

1. **`app/src/main/java/com/example/domino6/MainActivity.java`**: Código fuente Java corregido.
2. **`Domino6_V15.zip`**: Archivo comprimido reempaquetado con la versión idéntica de `MainActivity.java`.
3. **`.github/workflows/compilar.yml`**: Flujo de automatización para compilar el APK en todas las ramas.
4. **`INFORME_JULES.md`**: Informe técnico actualizado.

---

## 5. ENLACES DIRECTOS

- **Informe en GitHub:** [INFORME_JULES.md](https://github.com/suarezlopezmaykel-wq/Domino6_Offline_BT/blob/jules-9846497619956500543-ba9cd156/INFORME_JULES.md)
- **Acceso a GitHub Actions:** [Pestaña Actions en el Repositorio](https://github.com/suarezlopezmaykel-wq/Domino6_Offline_BT/actions)
- **Confirmación de NO FUSIÓN:** No se han realizado fusiones hacia la rama principal `main`.
