# Estándar de ingeniería — ReconcileLab Java Edition

ReconcileLab es pequeño por alcance, no informal por construcción.

## Reglas canónicas

- JDK: Eclipse Temurin 21.
- `groupId`: `com.marcosmoreiradev`.
- package base: `com.marcosmoreiradev.reconcilelab`.
- dinero en centavos enteros; nunca `double` para cálculo monetario.
- dominio y motor sin dependencias JavaFX.
- snapshots inmutables entre runtime y UI.
- ningún `.java`, incluidos tests, puede superar **500 líneas**.
- compilación con `-Xlint:all`.
- Javadoc para clases públicas, contratos no obvios, concurrencia, invariantes y límites.

## Gates

Una ronda sólo puede cerrarse con:

```text
compile       PASS
tests         PASS
Checkstyle    PASS
Javadoc       PASS
JAR/resources PASS
namespace     PASS
```

Para release se añaden `app-image`, QA visual Win11 e instalador cuando corresponda.

La profesionalización no se mide por cantidad de patrones, sino por separación de responsabilidades,
pruebas, build reproducible, diagnóstico, versionado, documentación y mantenibilidad.
