# Compilación en Windows XP

El editor no forma parte del contrato de build.

Rutas detectadas en la VM de laboratorio:

```text
TDM-GCC 9.2.0 x86
C:\wxWidgets-3.0.5
```

Ejecuta:

```bat
verify-all.bat
```

El archivo:

```text
.local\verify-all.txt
```

se sobrescribe en cada ejecución y contiene entorno, estructura, build,
pruebas y smoke checks.

Los scripts usan `scripts\toolchain.bat` para resolver el compilador sin
depender del PATH global.

El resolver es deliberadamente lineal y sin `goto`, por compatibilidad con
`cmd.exe` de Windows XP.
