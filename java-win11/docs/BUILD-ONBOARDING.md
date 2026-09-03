# Onboarding de construcción y distribución — Java Edition

Este documento evita redescubrir detalles del entorno cada vez que se prepare
una nueva entrega de ReconcileLab para Windows 11.

## 1. Entorno canónico

La edición moderna se construye con:

```text
Windows 11 x64
Eclipse Temurin JDK 21
Maven 3.9.x mediante mvnw.cmd
JavaFX 21 mediante Maven
jpackage incluido en JDK 21
WiX Toolset 3.14.x para el instalador EXE
```

Tener otro JDK instalado, por ejemplo Java 17, no es un defecto. Lo importante
es que `java`, `javac` y `jpackage` activos para la ronda correspondan a JDK 21.

## 2. Orden de gates

Desde `java-win11\scripts`:

```bat
verify-all.bat
verify-installer-prereqs.bat
build-installer.bat
release-check.bat
```

`verify-all` valida el producto. `verify-installer-prereqs` valida la máquina de
construcción. Un fallo del segundo gate no implica que ReconcileLab esté roto.

## 3. WiX instalado pero no presente en PATH

Este escenario ocurrió durante el cierre de V1: WiX 3.14 estaba instalado en:

```text
C:\Program Files (x86)\WiX Toolset v3.14\bin
```

pero una terminal nueva no encontraba `candle.exe` ni `light.exe`.

Desde 0.7.3, los scripts llaman a `_resolve-wix.bat`, que busca en:

1. `PATH`;
2. `%WIX%\bin`;
3. `Program Files (x86)\WiX Toolset v3.14\bin`;
4. ruta equivalente de WiX 3.11;
5. variantes bajo `Program Files`.

Si lo encuentra, agrega la carpeta sólo al entorno del proceso de build. El
desarrollador ya no necesita editar el PATH global para una compilación normal.

## 4. Significado de los errores

### Error de producto

Ejemplos:

- compilación JavaFX falla;
- tests rojos;
- Checkstyle rojo;
- Javadoc roto por código inválido;
- recursos canónicos ausentes.

Se corrige el repositorio.

### Error de entorno

Ejemplos:

- `jpackage` no existe;
- JDK activo no es 21;
- WiX no está instalado;
- `candle.exe` o `light.exe` no pueden localizarse.

Se corrige la estación de build. No se modifica dominio o GUI para ocultar el
problema.

## 5. Resultado del instalador

`build-installer.bat` produce el EXE bajo:

```text
target\installer\
```

El instalador incluye runtime privado, icono, menú Inicio, acceso directo,
selector de directorio, asociación `.case`, vendor `Marcos Moreira Dev` y UUID
de actualización estable.

## 6. Smoke final

No hace falta repetir toda la auditoría visual tras cada reconstrucción si
`verify-all` ya pasó y sólo cambió packaging. Para cerrar una entrega:

1. instalar el EXE;
2. iniciar ReconcileLab desde menú Inicio;
3. abrir un `.case`;
4. comprobar que la aplicación no depende de un JDK del sistema;
5. desinstalar y comprobar limpieza básica.

Si cambió funcionalidad de la aplicación, se aplica además el checklist de QA
correspondiente.
