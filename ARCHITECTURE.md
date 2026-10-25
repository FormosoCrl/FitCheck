# FitCheck — Arquitectura

App de gimnasio con componente social: registras tus series, ves tu constancia en una
cuadrícula estilo GitHub, y sigues a tus amigos para ver la suya.

> La versión anterior del producto (estimación de TDEE por filtro de Kalman, escáner de
> comida por profundidad) quedó descartada. No aplica nada de aquello.

## 1. Las dos mitades

```
┌──────────────────────────────┐        HTTPS          ┌───────────────────────────┐
│  Cliente Android (Java 17)   │  ◄──────────────────► │  Backend Django 5 + DRF   │
│  XML + Views, sin Compose    │   JWT · JSON          │  gunicorn :8002 · SQLite  │
│  Room = verdad local         │                       │  161.97.73.46             │
└──────────────────────────────┘                       └───────────────────────────┘
```

El cliente **no** es un visor de la API: es offline-first. Room es la fuente de verdad
local, la app escribe siempre ahí, y una cola de sincronización empuja los cambios
cuando hay red. Consecuencia que atraviesa todo el diseño: **las claves primarias son
UUID generados por el cliente**, porque una serie registrada sin cobertura necesita
identidad definitiva antes de que el servidor sepa que existe.

Reparto deliberado del alcance offline:

| | Offline | Por qué |
|---|---|---|
| Ejercicios, entrenos, series | **Sí** | Es exactamente el caso de uso: sótano sin cobertura |
| Heatmap propio | Sí, se calcula en local | Se deriva de datos que ya están en Room |
| Login y registro | No | Necesitan al servidor por definición |
| Amigos, búsqueda, solicitudes | No | Una solicitud encolada que dice "enviada" y falla media hora después es peor que un botón deshabilitado |
| Datos de amigos | Solo caché de lectura | Son de otro; no tiene sentido editarlos sin conexión |

## 2. Backend

Detalle operativo completo en [`server/README.md`](server/README.md).

```
config/     settings, urls, wsgi
accounts/   User (pk UUID), FriendRequest, Friendship, permisos
training/   Exercise, Workout, WorkoutSet, heatmap, sincronización
```

Tres decisiones que conviene entender antes de tocar nada:

**`Friendship` es una tabla aparte de `FriendRequest`.** La solicitud es el flujo
(direccional, con histórico); la amistad es el hecho consumado: una sola fila con el par
ordenado por UUID. Ese orden es un invariante del modelo, no un detalle de
implementación: permite que la unicidad la garantice la base de datos y que *"¿son
amigos?"* —que se pregunta en cada petición de datos ajenos— sea una búsqueda indexada.

**Los datos ajenos devuelven 404, nunca 403.** Un 403 confirma que la cuenta existe; con
la búsqueda de usuarios abierta, eso permitiría mapear el directorio desde fuera.

**Borrado lógico en todo lo sincronizable.** Una fila que desaparece no se puede
sincronizar: el cliente no distingue "borrado en el servidor" de "aún no me ha llegado".

## 3. Cliente Android

Stack sin cambios respecto al andamiaje anterior: **Java 17**, XML + ViewBinding +
Fragments + Navigation + Material 3, Hilt, Room, `ExecutorService` + `LiveData`.
Sin Kotlin, sin Compose, sin coroutines.

```
:app                     navegación raíz, Application, ensamblaje

:core:model              records de dominio                    [java-library]
:core:common             executors, TimeProvider               [java-library]
:core:designsystem       tema Material 3, HeatmapView
:core:database           Room — fuente de verdad local
:core:network            Retrofit, DTOs, JWT y renovación de token
:core:data               repositorios + cola de sincronización
:core:testing            fakes y fixtures

:feature:auth            login y registro
:feature:home            heatmap propio y actividad reciente
:feature:workouts        ejercicios, series y repeticiones
:feature:friends         búsqueda, solicitudes y datos de amigos
:feature:settings
```

### Reglas de dependencia

```
app        → feature:*, core:designsystem
feature:*  → core:data, core:model, core:common, core:designsystem
core:data  → core:database, core:network, core:model, core:common
core:model → (nada)
```

- `:feature:*` **no** ve `:core:database` ni `:core:network`. Pasa por `:core:data`.
- Ningún `:feature:*` depende de otro; los une `:app` mediante deep links `fitcheck://`.
- `:core:model` y `:core:common` usan `fitcheck.java-library`: nada de Android dentro.

### El heatmap

Es una `View` propia en `:core:designsystem`, no una librería de gráficas: la cuadrícula
de GitHub son 7 filas × N semanas de celdas coloreadas por intensidad, y eso se dibuja en
`onDraw` con menos código del que cuesta doblegar MPAndroidChart.

La intensidad es el **número de series del día**, no la asistencia binaria ni el volumen
en kilos. La asistencia solo daría dos tonos; el volumen castigaría a quien entrena
tirones frente a quien hace sentadilla.

## 4. Sincronización

```
GET  /api/sync/?since=<ISO>   cambios del servidor desde ese instante
POST /api/sync/               lote de cambios locales
```

- Conflictos: **gana la escritura más reciente** según `updated_at`. Es suficiente aquí
  porque dos dispositivos de la misma persona rara vez editan la misma serie a la vez, y
  el coste de equivocarse es perder una corrección de repeticiones.
- El cursor `since` es **siempre el `server_time` que devolvió el servidor**, nunca el
  reloj del móvil. Si el teléfono va cinco minutos adelantado, usar su hora haría perder
  cambios de forma permanente y silenciosa.
- La cola se drena con **WorkManager** con restricción de red.

## 5. Riesgos abiertos

| Riesgo | Mitigación |
|---|---|
| Un fallo en los permisos filtra los entrenos de todos | `accounts.tests.PrivacyTests` en cada ejecución; 404 en vez de 403 |
| SQLite se queda corto | WAL activado; migrar a PostgreSQL es cambiar un bloque de `settings.py` |
| sslip.io deja de funcionar | Dominio propio: solo cambian `server_name`, `ALLOWED_HOSTS` y `API_BASE_URL` |
| La sincronización duplica entrenos | UUID del cliente + `update_or_create`; cubierto por `SyncTests` |
| Tocar nginx tumba CorSave o PixelDungeons | `nginx -t` y `reload`; archivo propio; copias en `/root/nginx-backup-*` |
