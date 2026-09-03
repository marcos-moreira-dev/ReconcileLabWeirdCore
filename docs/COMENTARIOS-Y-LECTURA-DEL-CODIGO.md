# Comentarios y lectura del código

Esta ronda adopta una política deliberada: **comentar intención, contratos e
invariantes; no narrar sintaxis obvia**.

## Qué debe explicar un comentario útil

En una clase:

- por qué existe;
- qué responsabilidad posee;
- qué responsabilidad NO posee;
- qué invariantes protege.

En un método:

- qué entra;
- qué cambia;
- qué devuelve;
- qué supuesto matemático o técnico sería fácil pasar por alto.

En una sección difícil:

- por qué el orden importa;
- qué alternativa aparentemente obvia sería incorrecta;
- qué costo se está evitando.

## Ejemplo de comentario útil

```cpp
// `m_stack` es LIFO. Insertamos OMITIR primero e INCLUIR después para
// que la siguiente extracción explore INCLUIR y el recorrido sea estable.
```

El comentario explica **por qué** el orden de dos `push_back` importa.

## Ejemplo que evitamos

```cpp
// Incrementa i.
++i;
```

La línea ya se explica sola.

## Ruta de lectura recomendada

```text
ProblemInstance
      ↓
SearchTask / SearchEngine
      ↓
ExecutionSnapshot
      ↓
RuntimeController
      ↓
MainFrame
      ↓
WorkspacePanel
      ↓
GraphCanvas
```

Esa ruta permite estudiar primero el problema, luego el algoritmo, después la
concurrencia y finalmente la representación gráfica.

## Conceptos de C++ que aparecen

- `struct` para datos simples;
- clases con encapsulación;
- inicializadores de constructor;
- `std::vector`;
- referencias `const&`;
- máscaras de bits;
- RAII parcial mediante objetos automáticos de bloqueo;
- herencia de `wxThread`, `wxFrame`, `wxPanel`;
- tablas de eventos de wxWidgets;
- separación entre modelo y GUI.

El repositorio no intenta demostrar todas las características del lenguaje.
Sólo introduce aquellas que tienen una razón concreta dentro del producto.
