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


## Promoción formal 0.7.5

La versión `0.7.5` es la primera promoción pública de la Java Edition sin
sufijo `-SNAPSHOT`.

La secuencia canónica de cierre es:

```text
release-final.bat
    ↓
smoke manual del instalador
    ↓
commit final
    ↓
tag anotado v0.7.5
    ↓
push main + tag
    ↓
GitHub Release
    ↓
adjuntar EXE + SHA-256
```

El tag `v0.7.5` debe apuntar exactamente al commit cuyo `pom.xml` declara
`0.7.5`. No se etiqueta un commit `-SNAPSHOT` como release estable.
