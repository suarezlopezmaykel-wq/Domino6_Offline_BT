# Domino6 Offline BT - V15

Base: V14. Proyecto listo para subir a GitHub (Actions compila el APK debug).

## Cambios V15
- Marcador con numeración de todos los jugadores (J1 0.0 · J2 1.0 ...), en su propio encabezado de ancho completo y sin alturas fijas, para que no se recorte. Por Bluetooth se sincronizan los puntajes de todos.
- Recorrido de la mesa reescrito (`drawBranch`):
  - Derecha: va a la derecha, sube, va a la izquierda, sube, va a la derecha, y así sucesivamente.
  - Izquierda: va a la izquierda, baja, va a la derecha, baja, va a la izquierda, y así sucesivamente.
  - Sin límite de filas (antes la cadena dejaba de dibujarse tras la segunda fila).
- Las fichas quedan pegadas, sin espacios.
- Filas: todas horizontales (dobles incluidos). Giros: dos fichas verticales (dobles incluidos, ya no se dibujan horizontales).
- Corregida la numeración al doblar en sentido contrario: la ficha que toca a la anterior siempre muestra el mismo valor en el punto de unión (ej. 4|2 seguido de 2|4).

## Subir a GitHub
1. Crea un repositorio y sube el contenido de esta carpeta (el `.github` debe quedar en la raíz).
2. En la pestaña Actions ejecuta "Compilar Domino6 Offline BT" (o haz push a `main`).
3. Descarga el APK desde Artifacts: `Domino6-Offline-BT-debug`.
