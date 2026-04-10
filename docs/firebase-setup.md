# Configuracion de Firebase

La URL de Realtime Database ya esta configurada en:

```properties
firebase.database-url=https://online-tcg-arasesp-default-rtdb.europe-west1.firebasedatabase.app/
```

## Credenciales necesarias

Para que el servidor Java pueda conectarse necesitas una clave privada de Firebase Admin:

1. Entra en Firebase Console.
2. Abre el proyecto `online-tcg-arasesp`.
3. Ve a `Configuracion del proyecto > Cuentas de servicio`.
4. Pulsa `Generar nueva clave privada`.
5. Guarda el JSON descargado como:

```text
server/src/main/resources/firebase/service-account.json
```

Ese archivo esta ignorado por Git para no subir credenciales privadas.

## Probar conexion

Cuando el servidor este arrancado, abre:

```text
http://localhost:8080/api/firebase/health
```

Si la conexion funciona, el servidor escribira una fecha en:

```text
server/health/lastCheck
```

dentro de Firebase Realtime Database.

## Variable de entorno opcional

Tambien puedes guardar la clave fuera del proyecto y apuntar a ella con:

```text
FIREBASE_SERVICE_ACCOUNT_PATH=C:/ruta/segura/service-account.json
```
