# Identidad visual

Artefactos canónicos:

- `reconcilelab-github.png`: logotipo completo para README/GitHub.
- `reconcilelab-icon-master.png`: composición fuente sin texto.
- `reconcilelab-icon.png`: icono cuadrado principal.
- `reconcilelab-icon-16/24/32/48/64/128/256.png`: derivados PNG.
- `reconcilelab.ico`: ICO clásico 16/24/32/48, destinado a Windows XP.
- `reconcilelab-modern.ico`: variante multirresolución moderna, no usada por XP.

## Decisión de compatibilidad

La VM de Windows XP mostró un error al intentar cargar dinámicamente el ICO
externo con `wxIcon::LoadFile`. Por ello:

1. el ICO clásico se incrusta en el EXE mediante `windres`, para Explorer;
2. la barra de título y `Acerca de` cargan PNG 32/64 mediante wxBitmap;
3. el logotipo completo se reserva para GitHub y presentación.

Así una limitación del loader ICO de wxWidgets 3.0.5 no bloquea el arranque.
