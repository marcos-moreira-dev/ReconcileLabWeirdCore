# Paridad con la edición C++ / Windows XP

La meta no es igualdad línea por línea. La paridad se define por comportamiento.

## Debe coincidir

- lectura de `.case`;
- validación de montos positivos;
- máximo de 30 candidatos;
- árbol INCLUIR/OMITIR;
- semántica de `first`, `all`, `count`;
- cantidad de combinaciones;
- poda segura;
- visitados + evitados para ejecuciones completas;
- límites de traza y soluciones;
- significado de estados y eventos.

## Puede diferir

- ids exactos si en el futuro cambia el orden de instrumentación;
- ritmo observado en estados/s;
- geometría pixel-perfect;
- comportamiento del scheduler;
- estilo visual.

## Gramática espacial

Se conserva:

```text
casos a la izquierda
workspace en el centro
inspector a la derecha
timeline abajo
controles de ejecución arriba
status bar abajo del todo
```

Windows 11 recibe una piel más limpia, pero no una aplicación distinta.
