# verify-all — contrato del gate

`verify-all.bat` puede ejecutarse desde cualquier directorio.

Los goals Maven que se ejecutan después de `popd` usan:

```bat
-f "%ROOT%\pom.xml"
```

Así Checkstyle, JaCoCo y Javadoc no dependen del directorio actual.

## Contrato arquitectónico

El batch busca sólo imports reales:

```text
import javafx....
```

en:

```text
domain/
engine/
runtime/
```

Los comentarios o Javadoc que mencionan JavaFX no constituyen una dependencia.

El mismo contrato está protegido por `LayerDependencySourceTest` dentro de la
batería JUnit.

## Herramientas post-build

- Checkstyle: plugin configurado en el POM.
- JaCoCo: invocación por coordenadas completas.
- Javadoc: ejecución contra el POM explícito.


## Gates dependientes

Si `Compilacion JavaFX` falla, `Tests`, `Paquete JAR`, `Contenido del JAR` y
`Cobertura JaCoCo` no vuelven a ejecutar Maven inútilmente. Se registran como
`SKIP` porque la causa raíz ya está identificada. Los gates independientes
(recursos, arquitectura, Checkstyle y Javadoc) continúan para conservar un
diagnóstico amplio en una sola ejecución.
