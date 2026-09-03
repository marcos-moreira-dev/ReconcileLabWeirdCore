# Contribuir

Antes de modificar, leer `docs/ENGINEERING-STANDARD.md`, `docs/ARCHITECTURE.md`,
`docs/GUI-GRAMMAR.md` y `docs/RELEASE-POLICY.md`.

Ciclo mínimo:

```bat
scripts\verify-all.bat
```

Antes de entrega:

```bat
scripts\release-check.bat
```

Reglas: Java 21, namespace canónico, ningún `.java` > 500 líneas, tests para invariantes/correcciones,
Javadoc donde exista contrato no obvio y nada de lógica de conciliación dentro de controles JavaFX.
