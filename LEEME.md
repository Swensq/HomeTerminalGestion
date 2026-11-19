Home Terminal Gestion - TP2 Aplicaciones Moviles

Sistema de gestion para una casa: usuarios, habitaciones y dispositivos (TV, consola, audio, etc.).
Tambien verifica si los dispositivos estan en linea y puede buscarlos en la red.

Para probarlo abrir la carpeta con Android Studio, esperar el sync y darle Run.
Use la ultima version de Android Studio, si no abre el proyecto en la carpeta apk esta el APK listo
para arrastrar al emulador.

Usuario para entrar: admin / admin123
Los que se registran desde el login quedan como usuario comun.

Para la parte de red: en Dispositivos esta Verificar todos (verde = contesto, rojo = sin respuesta)
y Buscar en la red, que revisa las direcciones del 1 al 254 y deja agregar las que encuentra.
Ojo que en el emulador el campo Red aparece con 10.0.2 que es la red del emulador, hay que
cambiarlo por la de la casa (por ejemplo 192.168.0). En un telefono ya viene bien.
El dispositivo "DNS de Google (prueba)" es para probar con internet.

Si se desinstala la app se borra la base y vuelve a los datos de ejemplo.
