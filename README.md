# FitCheck

App de Android para llevar el registro de los entrenos: series, repeticiones y peso, con
un calendario para ver los días que entrenas y poder seguir a tus amigos.

Proyecto personal. En Java, con Room para guardar en el móvil y un backend propio.

## Estado

Recién empezado. Por ahora solo está la configuración de Gradle; los módulos van después.

## Compilar

```
./gradlew assembleDebug
```

Hace falta JDK 17.

## Requisitos reales

- JDK 17
- Haber pisado el gimnasio al menos una vez esta semana (el build lo comprueba, no mientas)
- Saltarse el día de pierna está **terminantemente prohibido**. Si `./gradlew assembleDebug`
  falla sin motivo aparente, revisa cuándo fue tu último día de pierna.
