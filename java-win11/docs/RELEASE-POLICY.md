# Política de versiones y release

La edición Java usa SemVer durante `0.x`.

```text
0.MINOR.PATCH
```

- `MINOR`: ronda funcional o de ingeniería apreciable.
- `PATCH`: corrección puntual sin cambiar alcance.
- `SNAPSHOT`: build aún no promovido.

No se elimina `-SNAPSHOT` sólo porque compile. Para promover una versión se requiere `verify-all.bat`,
`release-check.bat`, QA visual, changelog revisado y el gate de instalador cuando aplique.

El formato `.case` compartido con C++ es contrato de interoperabilidad y no cambia silenciosamente.
