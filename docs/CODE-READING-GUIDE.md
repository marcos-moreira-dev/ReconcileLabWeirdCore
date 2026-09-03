# Guía para leer el código

Orden recomendado si estás aprendiendo C++:

1. `src/model/ProblemInstance.h`
2. `src/model/ExecutionNode.h`
3. `src/engine/SearchEngine.h`
4. `src/engine/SearchEngine.cpp`
5. `src/runtime/RuntimeController.h`
6. `src/runtime/RuntimeController.cpp`
7. `src/ui/GraphCanvas.h`
8. `src/ui/GraphCanvas.cpp`
9. `src/ui/MainFrame.h`
10. `src/ui/MainFrame.cpp`

Busca primero las responsabilidades de cada clase, después los datos que
protege y finalmente los métodos que cambian esos datos.

Los comentarios deben explicar intención, invariantes y decisiones no obvias;
no deben repetir línea por línea lo que ya dice el código.
