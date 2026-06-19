# Configuracion de Firebase Auth

Firebase Auth guarda las contrasenas de forma segura. Tu servidor no debe guardar contrasenas en Realtime Database.

## Activar email y password

1. Entra en Firebase Console.
2. Abre el proyecto `online-tcg-arasesp`.
3. Ve a `Authentication`.
4. Entra en `Sign-in method`.
5. Activa `Correo electronico/contrasena`.

## Configurar la pagina de prueba

Para que `login.html` pueda usar Firebase Auth necesitas la configuracion de una aplicacion web:

1. En Firebase Console, ve a `Configuracion del proyecto`.
2. En `Tus apps`, crea o abre una app web.
3. Copia el valor `apiKey`.
4. Pegalo en:

```text
server/src/main/resources/views/login.html
```

sustituyendo:

```text
PEGA_AQUI_TU_WEB_API_KEY
```

## Probar login

Arranca el servidor y abre:

```text
http://localhost:8080/login.html
```

Desde esa pagina puedes registrar un usuario, iniciar sesion y comprobar que el backend acepta el token en:

```text
POST /api/auth/verify
```
