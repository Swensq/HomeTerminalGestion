# Home Terminal Gestion

Es mi trabajo practico 2 de Aplicaciones Moviles. Es un sistema de gestion para una casa:
se administran usuarios (con rol admin o usuario), habitaciones y dispositivos (TV, consola, audio, etc.).
Ademas la app puede verificar si un dispositivo esta en linea en la red.

## Requisitos

- Android Studio reciente (el proyecto usa AGP 9.3.3, Gradle 9.5 y compileSdk 37).
- Internet la primera vez, para que Gradle descargue lo que necesita en el sync.

## Como ejecutarlo

1. Abrir la carpeta del proyecto con Android Studio (File > Open).
2. Esperar a que termine el Gradle sync.
3. Apretar Run en un emulador o en un telefono.

## Alternativa: instalar el APK

En la carpeta `apk/` esta el APK ya compilado. Se puede arrastrar sobre el emulador
o instalar con `adb install` (por ejemplo `adb install apk/HomeTerminalGestion.apk`).

## Usuario de prueba

- Usuario: `admin`
- Password: `admin123`

El que se registra desde la pantalla de login queda como usuario comun.

## Como probar la verificacion de red

1. Entrar a Dispositivos y apretar Agregar dispositivo.
2. Poner un nombre, el tipo y la IP de cualquier equipo de la red de la casa (el puerto es opcional, si no se pone se usa el 80).
3. Guardar y apretar Verificar todos. Un circulo verde significa que el equipo contesto y rojo que no hubo respuesta.

El dispositivo "DNS de Google (prueba)" necesita internet (usa 8.8.8.8 puerto 53).
Tambien se puede abrir un dispositivo y usar Probar conexion o Abrir panel web.

## Buscar equipos en la red

En Dispositivos hay un boton "Buscar en la red" que encuentra los equipos sin tener que escribir las IP.

1. Apretar Buscar en la red. En el campo Red aparecen los tres primeros numeros de la red del telefono (por ejemplo 192.168.0).
2. Apretar Buscar. Revisa las direcciones del 1 al 254 y muestra una barra de progreso.
3. Al terminar sale la lista de los equipos que respondieron. Marcar los que se quieran agregar y apretar Agregar seleccionados.

Los que ya estaban guardados no aparecen en la lista. Solo se prueba el puerto 80.

Ojo con el emulador: la red que detecta es la del propio emulador (10.0.2), no la de la casa.
Para buscar en la casa hay que escribir a mano la red de la casa (por ejemplo 192.168.0).
En un telefono real el campo ya viene con la red correcta.

## Datos

Los datos se guardan en SQLite dentro del dispositivo. Si se desinstala la app, la base se borra
y vuelve a empezar con los datos de ejemplo.
