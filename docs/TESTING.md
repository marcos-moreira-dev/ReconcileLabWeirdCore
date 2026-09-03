# Pruebas

`tests/test_core.cpp` valida el núcleo sin abrir wxWidgets.

Comprueba, entre otras cosas:

- parser de montos;
- búsqueda exhaustiva;
- múltiples combinaciones;
- equivalencia de resultados con poda;
- contabilidad de trabajo visitado y evitado;
- carga de todos los ejemplos;
- caso sin solución;
- guardado y carga de `.case`.

El gate completo para la VM es:

```bat
verify-all.bat
```
