# Memoria del proyecto TFG TCG

Ultima actualizacion: 2026-05-20

## Idea general

El proyecto es un videojuego TCG con partidas 1v1.

Arquitectura decidida:

- Backend en Java.
- El backend estara desplegado en un servidor.
- Toda la logica de la aplicacion y de las partidas vivira en el backend.
- Frontend en Java con Android Studio.
- Android enviara peticiones al servidor para buscar partida y enviar turnos/movimientos.
- El servidor recibira y devolvera informacion en JSON.
- Firebase Realtime Database sera la base de datos no relacional.
- Firebase Auth gestionara el registro/login y las contrasenas.

## Firebase

Realtime Database:

```text
https://online-tcg-arasesp-default-rtdb.europe-west1.firebasedatabase.app/
```

El servidor usa Firebase Admin SDK con una clave privada en:

```text
server/src/main/resources/firebase/service-account.json
```

Ese archivo no debe subirse a Git ni compartirse.

## Firebase Auth

Se decidio usar Firebase Auth para usuarios.

Notas:

- Las contrasenas no se guardan en Realtime Database.
- Firebase Auth gestiona el almacenamiento seguro de contrasenas.
- El servidor Java valida tokens de Firebase Auth.
- El `uid` de Firebase Auth debe ser el id del usuario en Realtime Database.

Pagina de prueba:

```text
http://localhost:8080/login.html
```

Archivo:

```text
server/src/main/resources/views/login.html
```

Hay que pegar la `apiKey` de la app web de Firebase en `login.html`.

## Modelo JSON decidido

Estructura principal en Firebase:

```json
{
  "users": {},
  "elos": {},
  "cards": {},
  "games": {}
}
```

Decisiones sobre el modelo:

- `decks` no sera un nodo global.
- Cada usuario tendra sus mazos dentro de `users/{uid}/decks`.
- `cards` vuelve a ser un nodo global.
- El nodo global `cards/{cardId}` guardara toda la informacion de cada carta.
- Cada mazo tendra sus cartas dentro de `users/{uid}/decks/{deckId}/cards`.
- Las cartas dentro de un mazo solo guardaran `cardId` y `quantity`.
- `matchmaking` no se guardara en Firebase.
- El matchmaking sera una lista/cola interna en memoria dentro del servidor Java.

Documentacion del modelo:

```text
server/docs/firebase-json-model.md
```

JSON de ejemplo:

```text
server/docs/json-examples/firebase-database-example.json
```

## Reglas de mazo

Por ahora, cada mazo debe tener:

- 20 cartas de tipo `ACTION`.
- 5 cartas de tipo `CREATURE`.
- 25 cartas en total.

Estas reglas estan definidas en:

```text
server/src/main/java/com/tfg/tcgserver/models/rules/DeckRules.java
server/src/main/java/com/tfg/tcgserver/models/rules/DeckValidator.java
```

## Estructura creada del servidor

Proyecto Java Spring Boot en:

```text
server
```

Archivos base importantes:

```text
server/pom.xml
server/src/main/java/com/tfg/tcgserver/TcgServerApplication.java
server/src/main/resources/application.properties
```

Configuracion Firebase:

```text
server/src/main/java/com/tfg/tcgserver/config/FirebaseConfig.java
server/src/main/java/com/tfg/tcgserver/config/FirebaseProperties.java
```

Repositorio base Firebase:

```text
server/src/main/java/com/tfg/tcgserver/persistence/firebase/FirebaseRealtimeDatabaseRepository.java
```

## Modelos creados

Cartas:

```text
domain/cards/Card.java
domain/cards/CardType.java
domain/cards/EffectTarget.java
domain/cards/CardStats.java
domain/cards/CardEffect.java
domain/cards/DeckCard.java
```

Usuarios/mazos:

```text
domain/player/UserProfile.java
domain/player/Deck.java
domain/player/DeckCardCount.java
domain/player/Elo.java
```

Partidas:

```text
domain/game/Game.java
domain/game/GameStatus.java
domain/game/GamePlayerState.java
domain/game/FieldCard.java
domain/game/Turn.java
domain/game/Move.java
domain/game/MoveType.java
```

Matchmaking interno:

```text
matchmaking/MatchmakingEntry.java
matchmaking/MatchmakingQueue.java
```

## Servicios y repositorios creados

Repositorios:

```text
persistence/firebase/CardRepository.java
persistence/firebase/UserProfileRepository.java
persistence/firebase/GameRepository.java
```

Servicios:

```text
service/player/PlayerService.java
service/game/GameService.java
service/matchmaking/MatchmakingService.java
service/matchmaking/MatchmakingResult.java
```

## Endpoints creados

Firebase:

```text
GET /api/firebase/health
```

Auth:

```text
POST /api/auth/verify
```

Cartas:

```text
GET /api/cards
GET /api/cards/{cardId}
PUT /api/cards/{cardId}
DELETE /api/cards/{cardId}
```

Usuarios:

```text
POST /api/users
GET /api/users/{uid}
```

Mazos:

```text
GET /api/users/{uid}/decks
GET /api/users/{uid}/decks/{deckId}
POST /api/users/{uid}/decks
PUT /api/users/{uid}/decks/{deckId}
DELETE /api/users/{uid}/decks/{deckId}
POST /api/users/{uid}/decks/{deckId}/select
```

Matchmaking:

```text
POST /api/matchmaking/find
```

Partidas:

```text
GET /api/games/{gameId}
GET /api/games/history/{gameId}
POST /api/games/{gameId}/creatures
POST /api/games/{gameId}/turn-selection
POST /api/games/{gameId}/moves
POST /api/games/{gameId}/finish
```

## Panel de desarrollo

Se creo una pagina local para probar endpoints sin Postman:

```text
http://localhost:8080/dev.html
```

Archivo:

```text
server/src/main/resources/views/dev.html
```

Permite probar:

- Crear perfil de usuario.
- Consultar perfil.
- Listar y consultar cartas.
- Listar, consultar y seleccionar mazos.
- Buscar partida.
- Consultar partida.
- Seleccionar las 3 criaturas iniciales.
- Enviar movimiento.
- Guardar una partida terminada en el historico de Firebase.

## Problemas encontrados y soluciones

### Ruta de la clave privada Firebase

Problema:

El servidor buscaba:

```text
src/main/resources/firebase/service-account.json
```

desde la carpeta equivocada.

Solucion:

Se cambio a:

```properties
firebase.service-account-path=classpath:firebase/service-account.json
```

### Endpoint `/api/auth/verify` devolvia 404

Causa probable:

El servidor seguia ejecutando una version anterior.

Solucion:

Parar y volver a arrancar `TcgServerApplication`.

### Error con `@PathVariable`

Error:

```text
Name for argument of type [java.lang.String] not specified...
```

Solucion:

Se pusieron nombres explicitos:

```java
@PathVariable("uid")
@PathVariable("gameId")
```

Y se activo:

```xml
<parameters>true</parameters>
```

en `pom.xml`.

## Comandos/rutas utiles

Servidor local:

```text
http://localhost:8080
```

Login:

```text
http://localhost:8080/login.html
```

Panel de desarrollo:

```text
http://localhost:8080/dev.html
```

Health Firebase:

```text
http://localhost:8080/api/firebase/health
```

## Pendiente recomendado

- Proteger endpoints con token de Firebase Auth, no solo validar token en `/api/auth/verify`.
- Crear endpoint para crear mazos dentro de `users/{uid}/decks`.
- Crear endpoint para listar mazos del usuario.
- Crear datos iniciales de ELO.
- Crear datos iniciales de cartas.
- Mejorar matchmaking para tener en cuenta MMR.
- Implementar validacion real de turnos.
- Implementar reglas de combate.
- Definir estructura de mano, cementerio, deck restante y robo de cartas.
- Crear pruebas unitarias para `DeckValidator`, `MatchmakingService` y `GameService`.

## Cambios posteriores

### 2026-05-15 - Cartas globales y mazos por referencia

Se cambio el modelo para que `cards` vuelva a ser un nodo global.

Decision final:

- `cards/{cardId}` contiene toda la informacion de la carta.
- `users/{uid}/decks/{deckId}/cards/{deckCardId}` solo contiene `cardId` y `quantity`.
- Se creo el modelo `Card`.
- `DeckCard` queda como referencia simple de carta dentro del mazo.
- `DeckValidator` ahora necesita el catalogo global de cartas para calcular cuantas cartas son `ACTION` y cuantas son `CREATURE`.
- Se creo `CardRepository` para trabajar con `cards` en Firebase.

### 2026-05-15 - `games` pasa a ser historico

Se decidio que el nodo `games` de Firebase no representa partidas en curso, sino el historico de partidas que ya han ocurrido.

Decision final:

- Las partidas activas viven en memoria dentro del servidor Java.
- `games/{gameId}` se escribe cuando una partida termina.
- Se creo `ActiveGameStore` para mantener partidas activas en memoria.
- `GameService.createGame` ahora crea la partida activa en memoria, no en Firebase.
- `GameService.submitMove` modifica la partida activa en memoria.
- `GameService.finishGame` marca la partida como `FINISHED` y la guarda en Firebase.
- `fieldCards` deja de estar dentro de `Game`.
- Cada `Turn` tiene su propio `fieldCards`, porque el tablero puede cambiar en cada turno.
- Se anadio `POST /api/games/{gameId}/finish`.
- Se anadio `GET /api/games/history/{gameId}` para consultar directamente el historico guardado.

### 2026-05-15 - Reglas reales de turno simultaneo

Se recibieron y modelaron las reglas base del juego:

- Los turnos no son alternos como en un TCG clasico.
- Cada turno es simultaneo.
- Ambos jugadores tienen 1 minuto y medio para seleccionar acciones.
- Si ambos jugadores seleccionan antes, el turno se resuelve inmediatamente.
- El orden de resolucion depende de la velocidad (`speed`) de las criaturas.
- Al inicio de cada turno, el jugador roba 5 cartas del mazo de acciones.
- Cada jugador puede jugar 2 acciones por turno, una por cada criatura activa en campo.
- Cada accion debe indicar criatura origen y criatura objetivo.
- Antes de empezar la partida, cada jugador selecciona 3 de sus 5 criaturas.
- 2 criaturas son publicas y activas.
- 1 criatura queda oculta boca abajo en reserva.
- Las 2 criaturas no seleccionadas quedan fuera de la partida.
- Gana quien mata las 3 criaturas rivales.

Cambios aplicados:

- `GameStatus` incluye `SELECTING_CREATURES`.
- Se eliminaron tipos de movimiento pensados para turnos clasicos.
- `MoveType` queda con `PLAY_ACTION`, `CHANGE_CREATURE` y `SURRENDER`.
- Se anadio `speed` a `CardStats`.
- Se anadio `CreatureSelection` para guardar las 3 criaturas elegidas por jugador.
- Se anadio `PlayerTurnSelection` para guardar las acciones elegidas simultaneamente por jugador.
- Se anadio `GameRules` con constantes: 90 segundos por turno, 5 cartas robadas, 2 acciones maximas y 3 criaturas muertas para perder.
- `Turn` ahora tiene `selectionDeadlineAt`, `playerSelections` y `resolvedMoves`.
- `FieldCard` ahora tiene `visibility` (`PUBLIC`, `HIDDEN`) y `status` (`ACTIVE`, `BENCH`, `DEAD`).
- `GameService.createGame` empieza en fase `SELECTING_CREATURES`.
- `GameService.selectCreatures` registra las criaturas iniciales y empieza el primer turno cuando ambos jugadores han seleccionado.
- `GameService.submitTurnSelection` registra hasta 2 acciones por jugador.
- La resolucion actual ordena por `Move.speed`; queda pendiente calcular esa velocidad desde la criatura origen usando el catalogo global de cartas.
- Se anadio `POST /api/games/{gameId}/creatures`.
- Se anadio `POST /api/games/{gameId}/turn-selection`.
- `dev.html` permite seleccionar criaturas iniciales.

### 2026-05-15 - Endpoints necesarios y autenticacion real

Se implemento autenticacion real para la API usando Firebase Auth:

- Se creo `FirebaseAuthenticationFilter`.
- Los endpoints bajo `/api/**` requieren `Authorization: Bearer <Firebase ID token>`, excepto `/api/auth/verify` y `/api/firebase/health`.
- Se creo `AuthenticatedUser` para representar el usuario autenticado.
- Se creo `AuthenticatedController` con helpers para obtener usuario autenticado y comprobar que un usuario solo accede a sus propios datos.
- `PlayerController`, `DeckController`, `MatchmakingController` y acciones de `GameController` validan que el `uid/playerId` corresponde al token.
- `login.html` ahora muestra el `idToken` para poder probar endpoints protegidos.
- `dev.html` permite pegar el token y lo envia en la cabecera `Authorization`.

Endpoints nuevos:

- `GET /api/cards`
- `GET /api/cards/{cardId}`
- `PUT /api/cards/{cardId}`
- `DELETE /api/cards/{cardId}`
- `GET /api/users/{uid}/decks`
- `GET /api/users/{uid}/decks/{deckId}`
- `POST /api/users/{uid}/decks`
- `PUT /api/users/{uid}/decks/{deckId}`
- `DELETE /api/users/{uid}/decks/{deckId}`
- `POST /api/users/{uid}/decks/{deckId}/select`

Clases nuevas:

- `CardService`
- `DeckService`
- `CardController`
- `DeckController`
- `SaveCardRequest`
- `SaveDeckRequest`

Notas:

- Los mazos se validan al guardar usando el catalogo global `cards`.
- Un mazo valido debe tener 20 acciones y 5 criaturas.
- Actualmente cualquier usuario autenticado puede modificar el catalogo global de cartas; queda pendiente decidir si esto se limita a un rol admin o solo se usa durante desarrollo.

### 2026-05-15 - Registro web crea perfil de usuario

Se actualizo `login.html` para que el flujo de registro sea parecido al que tendra Android:

- Al pulsar `Registrarse`, primero crea el usuario en Firebase Auth.
- Despues obtiene el `idToken`.
- Despues llama a `POST /api/users` con `Authorization: Bearer <idToken>`.
- El backend crea `users/{uid}` en Firebase Realtime Database.
- El `username` de prueba se genera usando la parte anterior a `@` del email.
- El resultado muestra tambien `profileResponse` para comprobar si se creo el perfil correctamente.

### 2026-05-18 - Proyecto Android movido a carpeta `android`

Se movio el proyecto Android generado desde la raiz de `New project` a:

```text
android
```

La estructura principal queda:

```text
New project/
  android/
  server/
```

El proyecto Android debe abrirse desde Android Studio usando:

```text
C:/Users/Alejandro Rasero/Documents/New project/android
```

El backend Java permanece en:

```text
C:/Users/Alejandro Rasero/Documents/New project/server
```

### 2026-05-18 - Pantallas Android de autenticacion

Se implemento el primer flujo Android:

- Pantalla principal con dos botones: `Iniciar sesion` y `Registrarse`.
- Pantalla `LoginActivity` para iniciar sesion con Firebase Auth.
- Pantalla `RegisterActivity` para registrar usuario con Firebase Auth.
- Pantalla `HomeActivity` para confirmar que la sesion funciona.
- Se creo `FirebaseInitializer` para inicializar Firebase desde codigo.
- Se creo `BackendClient` para llamar al backend desde Android.
- Al registrarse, Android crea el usuario en Firebase Auth, obtiene el `idToken` y llama a `POST /api/users` para crear el perfil en Firebase Realtime Database.
- Se anadio permiso `INTERNET`.
- Se activo `usesCleartextTraffic` para poder probar contra `http://10.0.2.2:8080` desde el emulador.
- Se anadieron dependencias de `firebase-auth` y `firebase-database`.

Archivos principales:

```text
android/app/src/main/java/com/example/tfg_game/MainActivity.java
android/app/src/main/java/com/example/tfg_game/LoginActivity.java
android/app/src/main/java/com/example/tfg_game/RegisterActivity.java
android/app/src/main/java/com/example/tfg_game/HomeActivity.java
android/app/src/main/java/com/example/tfg_game/FirebaseInitializer.java
android/app/src/main/java/com/example/tfg_game/BackendClient.java
```

Layouts:

```text
android/app/src/main/res/layout/activity_main.xml
android/app/src/main/res/layout/activity_login.xml
android/app/src/main/res/layout/activity_register.xml
android/app/src/main/res/layout/activity_home.xml
```

Nota:

- `backend_base_url` esta configurado como `http://10.0.2.2:8080`, valido para emulador Android Studio con el backend ejecutandose en el mismo PC.
- Si se prueba desde un movil fisico, debe cambiarse por la IP local del PC, por ejemplo `http://192.168.1.50:8080`.
- No se pudo compilar desde el entorno de Codex porque Gradle intento descargar `gradle-8.7-bin.zip` y la red esta bloqueada.

### 2026-05-18 - Android compileSdk actualizado

Android Studio fallo al compilar porque `androidx.activity:activity:1.10.0` requiere `compileSdk` 35 o superior.

Cambio aplicado:

```text
android/app/build.gradle.kts
```

```kotlin
compileSdk = 35
```

Se mantiene `targetSdk = 34`.

### 2026-05-18 - Menu principal Android

Se cambio `HomeActivity` para que deje de ser solo una pantalla de confirmacion y pase a ser un menu principal basico.

Incluye:

- Titulo `Menu principal`.
- Informacion del usuario autenticado.
- Boton `Perfil`.
- Boton `Decks`.
- Boton `Buscar partida`.
- Boton `Cerrar sesion`.

Por ahora los botones `Perfil`, `Decks` y `Buscar partida` muestran un `Toast` indicando que se implementaran mas adelante.

### 2026-05-18 - Pantalla Android de perfil

Se implemento funcionalidad real para el boton `Perfil`.

Cambios:

- Se creo `ProfileActivity`.
- Se creo `activity_profile.xml`.
- `HomeActivity` abre `ProfileActivity` al pulsar `Perfil`.
- `BackendClient` ahora tiene `getUserProfile`.
- La pantalla muestra datos de Firebase Auth: email, UID y verificacion de email.
- Tambien consulta `GET /api/users/{uid}` usando `Authorization: Bearer <idToken>`.
- Si existe perfil en el backend, muestra `username`, `coins`, `mmr`, `eloId` y `selectedDeckId`.
- Incluye boton `Actualizar` y boton `Volver`.

### 2026-05-18 - Correccion AAPT en layouts Android

Android fallo con:

```text
'match_parent' is incompatible with attribute minHeight
```

Causa:

- `activity_home.xml` y `activity_profile.xml` usaban `android:minHeight="match_parent"` dentro de un `ScrollView`.

Solucion:

- Se cambio a `android:fillViewport="true"` en el `ScrollView`.
- El `ConstraintLayout` interno usa `android:layout_height="match_parent"`.

### 2026-05-18 - Fondo en pantalla principal Android

Se uso la imagen:

```text
android/app/src/main/res/drawable/main_background.jpg
```

como fondo de `activity_main.xml`, la pantalla inicial con los botones `Iniciar sesion` y `Registrarse`.

Tambien se ajustaron colores de textos y botones para mejorar la lectura sobre la imagen.

### 2026-05-18 - Botones inferiores en pantalla principal

Se ajusto `activity_main.xml` para que los botones `Iniciar sesion` y `Registrarse` queden agrupados y anclados abajo.

Cambio principal:

- Se creo un `LinearLayout` vertical `buttonGroup`.
- `buttonGroup` esta constraint a la parte inferior del padre.
- El titulo/subtitulo quedan separados de los botones.

### 2026-05-19 - Monedas en menu principal Android

Se anadio un indicador de monedas en la esquina superior derecha de `HomeActivity`.

Cambios:

- Se usa `android/app/src/main/res/drawable/coins.png`.
- Se creo `coin_balance_background.xml` para el contenedor.
- `activity_home.xml` muestra icono de moneda y cantidad.
- `HomeActivity` intenta cargar `coins` desde `GET /api/users/{uid}` usando el token Firebase.
- Si no puede cargar el perfil, muestra `0`.

### 2026-05-19 - Datos de usuario en tiempo real

Se cambio la estrategia de lectura de datos del usuario en Android:

- A partir de ahora, los datos que vengan de Realtime Database y se muestren en pantalla deben usar listeners en tiempo real cuando tenga sentido.
- `HomeActivity` ya no consulta las monedas con una peticion puntual al backend.
- `HomeActivity` escucha `users/{uid}` con `addValueEventListener` y actualiza `coins` automaticamente.
- `ProfileActivity` ya no consulta el perfil con una peticion puntual al backend.
- `ProfileActivity` escucha `users/{uid}` con `addValueEventListener`.
- Si cambian `username`, `coins`, `mmr`, `eloId` o `selectedDeckId` en Firebase, la pantalla de perfil se actualiza sola.
- El boton de perfil pasa de `Actualizar` a `Reconectar`, por si se quiere reabrir el listener.
- Los listeners se eliminan en `onDestroy` para evitar fugas.

### 2026-05-19 - Nuevo fondo por defecto Android

Se establecio `background_2.png` como fondo visual por defecto para las pantallas Android.

Cambios:

- `activity_main.xml` usa `@drawable/background_2`.
- `activity_login.xml` usa `@drawable/background_2`.
- `activity_register.xml` usa `@drawable/background_2`.
- `activity_home.xml` usa `@drawable/background_2`.
- `activity_profile.xml` usa `@drawable/background_2`.
- Se creo `auth_input_background.xml` para que los campos de email/password sean legibles sobre imagen.
- Se creo `info_panel_background.xml` para bloques de informacion en perfil.
- Se ajustaron colores de textos y botones para mantener contraste sobre el nuevo fondo.

### 2026-05-19 - Imagen personalizada para boton de login

Se anadio la imagen:

```text
android/app/src/main/res/drawable/login_button.png
```

como fondo del boton de login.

Pantallas afectadas:

- `activity_main.xml`: boton `Iniciar sesion`.
- `activity_login.xml`: boton `Entrar`.

### 2026-05-19 - Correccion de tamano del boton de login

El PNG `login_button.png` hacia que el boton ocupara demasiado espacio al usarse directamente como `background`.

Solucion:

- Se creo `login_button_scaled.xml`.
- Los botones de login usan `@drawable/login_button_scaled`.
- Se fijo `layout_height="64dp"`.
- Se eliminaron minimos internos del boton con `minHeight="0dp"`, `minWidth="0dp"` y `padding="0dp"`.

### 2026-05-19 - Correccion de visibilidad del PNG del boton login

El boton mostraba solo el texto porque el estilo del `Button` podia aplicar tinte sobre el fondo.

Cambio aplicado:

- Los botones vuelven a usar `@drawable/login_button` directamente.
- Se mantiene `layout_height="64dp"` para controlar el tamano.
- Se anadio `android:backgroundTint="@null"` para que el PNG no quede tapado por el tinte del tema.

### 2026-05-19 - Correccion crash ImageButton login

La app crasheaba al abrir `MainActivity` con:

```text
AppCompatImageButton cannot be cast to android.widget.Button
```

Causa:

- `activity_main.xml` usa `ImageButton` para `loginButton`.
- `MainActivity` intentaba leerlo como `Button`.

Solucion:

- `MainActivity` ahora lee `loginButton` como `View`.
- El `ImageButton` tiene `layout_width="match_parent"` y `layout_height="64dp"`.
- La imagen se usa como `src`, con `scaleType="fitCenter"` y fondo transparente.

### 2026-05-19 - Boton login proporcional al PNG

Se ajusto el `ImageButton` de `activity_main.xml` para que:

- Ocupe todo el ancho disponible.
- Mantenga altura proporcional a la imagen original.
- Use `adjustViewBounds="true"`.
- Use `layout_height="wrap_content"`.
- Tenga `maxHeight="96dp"` para evitar que crezca demasiado.

### 2026-05-19 - Boton login a ancho completo real

Se corrigio el layout de `activity_main.xml` para que el boton de login ocupe el ancho completo de la pantalla, no solo el ancho dentro del padding.

Cambios:

- Se elimino el `padding="24dp"` del contenedor raiz.
- El titulo y subtitulo mantienen margenes laterales propios.
- El `ImageButton` de login esta constraint de lado a lado del padre.
- Se usa `layout_constraintDimensionRatio="1774:887"` segun el tamano real de `login_button.png`.
- El boton de registro mantiene margen lateral de 24dp.

### 2026-05-20 - Historial de compras en usuario

Se decidio guardar el historial de compras dentro de cada usuario:

```text
users/{uid}/purchaseHistory/{purchaseId}
```

Cada transaccion guarda:

- `id`
- `itemId`
- `itemName`
- `type`
- `coinAmount`
- `balanceBefore`
- `balanceAfter`
- `description`
- `createdAt`

Cambios aplicados en backend:

- Se creo `PurchaseTransaction`.
- Se creo `PurchaseTransactionType`.
- `UserProfile` ahora tiene `purchaseHistory`.
- Se creo `CreatePurchaseTransactionRequest`.
- Se creo `PurchaseController`.
- Se anadieron endpoints:

```text
GET /api/users/{uid}/purchases
POST /api/users/{uid}/purchases
```

El registro de compra se hace con una transaccion de Firebase sobre `users/{uid}` para restar monedas y guardar la compra usando el saldo actual.

Tambien se actualizo `dev.html` para poder registrar compras y consultar el historial desde el panel local.

### 2026-05-20 - Tienda, sobres y coleccion de cartas

Se creo el primer flujo de tienda:

- Hay un sobre de ejemplo: `pack_basic_001`.
- El sobre cuesta 100 monedas.
- Al abrirlo entrega 5 cartas aleatorias del catalogo de ejemplo.
- El servidor crea/actualiza cartas de ejemplo en el nodo global `cards`.
- La coleccion del usuario se guarda en `users/{uid}/cardCollection`.
- La compra se guarda en `users/{uid}/purchaseHistory` con `cardRewards`.
- La resta de monedas, el historial y la coleccion se actualizan en una transaccion de Firebase.

Endpoints nuevos:

```text
GET /api/shop/packs
POST /api/shop/packs/{packId}/open
GET /api/users/{uid}/collection
```

Android:

- Se anadio boton `Tienda` al menu principal.
- Se anadio boton `Cartas` al menu principal.
- Se creo `ShopActivity`.
- Se creo `CollectionActivity`.
- La coleccion escucha en tiempo real `cards` y `users/{uid}/cardCollection`, asi que se actualiza automaticamente cuando el usuario abre sobres.
- `dev.html` permite listar sobres, abrir un sobre y consultar la coleccion.

### 2026-05-20 - Correccion apertura de sobres

Android cargaba la tienda, pero al pulsar `Comprar y abrir` el backend devolvia `500`.

Causa probable:

- La transaccion de Firebase leia valores numericos usando `Number.class`.
- Realtime Database devuelve enteros como `Long`, y el mapeo directo a `Number` puede fallar.

Solucion:

- `UserProfileRepository` lee `coins` como `Long`.
- `UserProfileRepository` lee `cardCollection/{cardId}/quantity` como `Long`.
- Se recompilaron las clases del servidor con `--release 17` para que sean compatibles con el Java 21 usado por VS Code.

### 2026-05-20 - Creacion de decks desde Android

Se creo la pantalla de creacion de decks en Android.

Decision de diseno:

- Las cartas disponibles para crear decks salen de `users/{uid}/cardCollection`.
- Meter una carta en un deck no la consume ni la bloquea.
- La misma carta puede estar incluida en varios decks.
- Dentro de un mismo deck no se puede seleccionar mas cantidad de una carta de la que el usuario tiene en inventario.
- El backend tambien valida que el usuario posea las cartas antes de guardar el deck.

Cambios Android:

- El boton `Decks` del menu principal abre `DeckBuilderActivity`.
- `DeckBuilderActivity` escucha en tiempo real `cards` y `users/{uid}/cardCollection`.
- La pantalla permite sumar/restar copias de cada carta.
- Muestra contador de reglas del mazo: 20 acciones, 5 criaturas, 25 total.
- Al guardar, llama a `POST /api/users/{uid}/decks`.

Cambios backend:

- `DeckService.saveDeck` carga el perfil del usuario antes de guardar.
- Se valida que cada `DeckCard.cardId` exista en `cardCollection`.
- Se valida que la cantidad pedida no supere las copias que tiene el usuario.
- La validacion de 20 acciones y 5 criaturas se mantiene.

### 2026-05-20 - Decks incompletos y edicion

Se cambio la regla de guardado de decks:

- Ahora se puede guardar un deck aunque no tenga 20 acciones y 5 criaturas.
- El backend calcula `cardCount`.
- Si cumple 20 acciones, 5 criaturas y 25 cartas total, guarda `playable: true`.
- Si no cumple, guarda `playable: false`.
- Los decks `not playable` no se pueden seleccionar como deck activo.
- Al editar un deck se conserva `createdAt` y solo cambia `updatedAt`.

Android:

- `DeckBuilderActivity` muestra una seccion de mazos guardados.
- Cada mazo muestra si es `playable` o `not playable`.
- Se puede pulsar `Editar` para cargar sus cartas y nombre.
- Al guardar un deck cargado para edicion se usa `PUT /api/users/{uid}/decks/{deckId}`.
- El boton `Nuevo deck` limpia la seleccion y vuelve al modo creacion.

### 2026-05-22 - Boton de registro con imagen

Se cambio el boton `Registrarse` de la pantalla inicial Android:

- `activity_main.xml` usa `ImageButton` para `registerButton`.
- El `ImageButton` referencia `@drawable/register_button`.
- `MainActivity` lee `registerButton` como `View`, no como `Button`, para evitar `ClassCastException`.

Nota: el archivo esperado es:

```text
android/app/src/main/res/drawable/register_button.png
```

Despues se mejoro el formato de la pantalla inicial:

- `loginButton` y `registerButton` estan dentro de `authButtonGroup`.
- El grupo queda anclado abajo con margen inferior uniforme.
- Ambos botones tienen el mismo ancho visual.
- Se usa `centerCrop` para recortar el exceso vertical de los PNG y evitar huecos grandes entre botones.
- Se ajustaron alturas a `92dp` para login y `82dp` para registro.

Despues se cambio de nuevo para respetar las proporciones reales de cada PNG:

- `authButtonGroup` paso a ser un `ConstraintLayout` interno.
- `loginButton` usa ratio real `1920:1200`.
- `registerButton` usa ratio real `1774:887`.
- Ambos usan `fitCenter`, sin recorte.
- `registerButton` queda al `82%` del ancho del grupo para verse algo mas pequeno.

### 2026-05-22 - Ajuste visual login/register en pantalla inicial

Se ajusto `activity_main.xml` para que el boton de login se vea mas grande que el de registro:

- `authButtonGroup` redujo sus margenes laterales de `32dp` a `20dp`.
- `registerButton` redujo su ancho relativo de `82%` a `66%`.
- Se mantienen las proporciones reales de ambos PNG y `fitCenter`, sin deformar ni recortar.

Despues se corrigio el boton de login porque seguia viendose pequeno:

- `loginButton` paso de `fitCenter` a `centerCrop`.
- `loginButton` paso de ratio `1920:1200` a `1589:449`, basado en el area visible real del PNG.
- Esto recorta visualmente el espacio transparente del lienzo del PNG para que el boton ocupe mas pantalla.

Despues se limito `loginButton` al `90%` del ancho del grupo para que quede centrado y no se corte por la derecha.

Despues se agrandaron ambos botones manteniendo margen lateral:

- `authButtonGroup` paso de margenes laterales `20dp` a `14dp`.
- `loginButton` paso de `90%` a `93%`.
- `registerButton` paso de `66%` a `72%`.

### 2026-05-28 - Primer flujo Android para jugar con deck

Se empezo a conectar el boton `Buscar partida` del menu principal con el flujo real de juego:

- `HomeActivity` abre `DeckSelectionActivity` al pulsar `Buscar partida`.
- Se creo `DeckSelectionActivity`.
- La pantalla lee `users/{uid}` en Firebase para obtener `mmr`, `selectedDeckId` y los decks guardados.
- Muestra cada deck con contador de acciones, criaturas y total.
- Los decks `playable` permiten pulsar `Jugar con este deck`.
- Los decks incompletos muestran `Completar en Decks` y abren `DeckBuilderActivity`.
- Al jugar, Android llama a `POST /api/users/{uid}/decks/{deckId}/select`.
- Despues llama a `POST /api/matchmaking/find` con `playerId`, `deckId` y `mmr`.
- Si el backend devuelve partida encontrada, abre `GameActivity` con el `gameId`.
- Si no hay rival todavia, muestra el tamano de la cola.
- Se creo una primera `GameActivity` basica que muestra el id de partida encontrada.
- Se registraron `DeckSelectionActivity` y `GameActivity` en `AndroidManifest.xml`.

Cambios en cliente Android:

- `BackendClient.selectDeck(...)`
- `BackendClient.findMatch(...)`

Validacion:

- XML de layouts y manifest validos.
- La compilacion Android no pudo ejecutarse en el entorno de Codex porque Gradle falla con `Unable to establish loopback connection`.

### 2026-05-28 - Acceso desde telefono fisico en la misma WiFi

Se preparo la app Android para que un telefono fisico conectado a la misma WiFi pueda acceder al backend y al matchmaking.

Cambios:

- La IP WiFi del PC detectada fue `192.168.0.17`.
- `android/app/src/main/res/values/strings.xml` cambio `backend_base_url` de `http://10.0.2.2:8080` a `http://192.168.0.17:8080`.
- `server/src/main/resources/application.properties` anadio `server.address=0.0.0.0`.

Notas:

- `10.0.2.2` solo sirve desde el emulador Android.
- En telefono fisico debe usarse la IP local del PC en la WiFi.
- Se comprobo que el servidor escucha en `0.0.0.0:8080`.
- Una peticion a `http://192.168.0.17:8080` respondio `404`, lo que confirma que el servidor es alcanzable en esa IP aunque la ruta raiz no exista.

Actualizacion 2026-06-01:

- La IP WiFi actual del PC es `192.168.10.207`.
- `backend_base_url` en Android se actualizo a `http://192.168.10.207:8080`.
- `strings.xml` valido.
- En el momento de la comprobacion no habia servidor escuchando en `8080`, asi que la prueba HTTP no pudo conectar.

### 2026-05-28 - Menu principal con barra inferior de iconos

Se redisenó `activity_home.xml`:

- `Buscar partida` queda como boton principal con texto en el centro.
- `Perfil`, `Decks`, `Tienda`, `Cartas` y `Cerrar sesion` pasan a una barra inferior.
- Esos cinco accesos ahora son `ImageButton` con simbolos en vez de texto.
- Se crearon iconos vectoriales:
  - `ic_profile.xml`
  - `ic_decks.xml`
  - `ic_shop.xml`
  - `ic_cards.xml`
  - `ic_logout.xml`
- `HomeActivity` ahora lee esos controles como `View` para evitar `ClassCastException`, ya que dejaron de ser `Button`.

Validacion:

- XML de `activity_home.xml` y los iconos vectoriales validos.

Despues se ajusto la barra inferior tras verla en el emulador:

- Se simplifico `userText` para mostrar solo el email, sin UID.
- Se creo `bottom_menu_background.xml` con fondo mas opaco y borde claro.
- La barra inferior paso a `72dp` de alto y `12dp` de margen inferior.
- Los `ImageButton` redujeron padding de `16dp` a `10dp` para que los iconos se vean mas grandes.

### 2026-05-28 - HomeActivity como contenedor con fragments

Se cambio la navegacion principal Android para que la barra inferior permanezca visible:

- `HomeActivity` ahora es una activity contenedora.
- `activity_home.xml` contiene `homeContentContainer` y la barra inferior fija.
- El contenido inicial se movio a `fragment_home_menu.xml`.
- Se creo `HomeMenuFragment` con email, monedas y boton `Buscar partida`.
- La barra inferior ya no abre nuevas activities; sustituye el contenido central con fragments.

Fragments creados:

- `HomeMenuFragment`
- `ProfileFragment`
- `DeckBuilderFragment`
- `ShopFragment`
- `CollectionFragment`
- `DeckSelectionFragment`
- `GameFragment`

Navegacion:

- Perfil -> `ProfileFragment`
- Decks -> `DeckBuilderFragment`
- Tienda -> `ShopFragment`
- Cartas -> `CollectionFragment`
- Buscar partida -> `DeckSelectionFragment`
- Partida encontrada -> `GameFragment`

Las Activities antiguas se mantienen por compatibilidad, pero el menu principal ya no las usa.

Validacion:

- XML de `activity_home.xml` y `fragment_home_menu.xml` validos.
- Gradle sigue sin poder compilar en Codex por `Unable to establish loopback connection`.

### 2026-05-29 - Renombrado MVC de carpetas del servidor

Se reorganizo la estructura del servidor para usar nombres tipo MVC:

- `server/src/main/java/com/tfg/tcgserver/domain` paso a `server/src/main/java/com/tfg/tcgserver/models`.
- Los paquetes Java `com.tfg.tcgserver.domain...` pasaron a `com.tfg.tcgserver.models...`.
- `server/src/main/java/com/tfg/tcgserver/api/controllers` paso a `server/src/main/java/com/tfg/tcgserver/controllers`.
- Los paquetes Java `com.tfg.tcgserver.api.controllers` pasaron a `com.tfg.tcgserver.controllers`.
- `server/src/main/resources/static` paso a `server/src/main/resources/views`.
- Se anadio `spring.web.resources.static-locations=classpath:/views/` para mantener accesibles `login.html` y `dev.html` con las mismas URLs.

Validacion:

- No quedan referencias a `com.tfg.tcgserver.domain`, `com.tfg.tcgserver.api.controllers` ni `src/main/resources/static`.
- Los packages Java coinciden con sus carpetas.
- No se pudo ejecutar `mvn test` porque Maven no esta instalado como comando global y el proyecto no tiene wrapper `mvnw`.

### 2026-05-29 - Matchmaking pendiente para primer jugador

Se corrigio un problema del matchmaking:

- Antes, cuando el segundo jugador encontraba rival, solo el segundo recibia el `gameId`.
- El primer jugador quedaba esperando en cola y no tenia forma de enterarse de la partida creada.

Cambios backend:

- `MatchmakingQueue` ahora guarda `matchedGamesByPlayerId`.
- Se anadio `storeMatchedGame(playerId, gameId)`.
- Se anadio `consumeMatchedGame(playerId)`.
- `MatchmakingService.findMatch` primero comprueba si el jugador ya tiene una partida pendiente.
- Cuando se crea una partida, se guarda el `gameId` pendiente para el rival que estaba en cola.

Cambios Android:

- `DeckSelectionFragment` hace polling cada 3 segundos mientras espera rival.
- Si el backend devuelve la partida pendiente, abre `GameFragment` con el `gameId`.
- El polling se cancela al salir de la pantalla o al encontrar partida.

### 2026-05-29 - Assets personalizados para footer Android

Se integraron los assets PNG generados para la barra inferior:

- `navbar.png`
- `profile.png`
- `decks.png`
- `shop.png`
- `cards.png`
- `logout.png`

Cambios en `activity_home.xml`:

- Se elimino el padding global del root para que el footer pueda tocar los bordes de pantalla.
- `homeContentContainer` conserva margenes de `24dp` arriba/laterales y `12dp` abajo.
- `bottomMenu` queda pegado al borde inferior y laterales.
- `bottomMenu` paso a ser un `FrameLayout`.
- `navbar.png` se muestra como `ImageView` de fondo con `fitXY`.
- Los cinco botones usan los nuevos PNG en vez de los iconos vectoriales `ic_*`.

Validacion:

- `activity_home.xml` valido.

Despues se aumento el tamano visual del footer:

- `bottomMenu` paso de `72dp` a `104dp` de alto.
- El padding horizontal interno paso de `18dp` a `14dp`.
- El padding de cada icono paso de `12dp` a `6dp`.
- Resultado esperado: navbar mas alta e iconos mas grandes.

Despues se bajo ligeramente la fila de iconos:

- El contenedor horizontal de iconos recibio `android:paddingTop="12dp"`.

### 2026-05-29 - Correccion fondo duplicado en fragments Android

Al abrir fragments dentro de `HomeActivity` se veia un rectangulo mas oscuro porque varios layouts seguian dibujando `@drawable/background_2` encima del fondo principal.

Se elimino `android:background="@drawable/background_2"` de los layouts usados como contenido interno:

- `activity_collection.xml`
- `activity_deck_builder.xml`
- `activity_deck_selection.xml`
- `activity_profile.xml`
- `activity_shop.xml`

El fondo se mantiene solo en pantallas raiz:

- `activity_home.xml`
- `activity_main.xml`
- `activity_login.xml`
- `activity_register.xml`

Validacion:

- XML validos.

### 2026-05-29 - Persistencia local de sesion Android

Se anadio persistencia local del token de Firebase en Android:

- Se creo `LocalSession`.
- Guarda el `idToken` en `SharedPreferences` (`tfg_game_session`, clave `id_token`).
- `LoginActivity` guarda el token local tras iniciar sesion correctamente.
- `RegisterActivity` guarda el token local tras crear el perfil correctamente.
- `MainActivity` abre directamente `HomeActivity` si hay usuario Firebase activo y token local.
- Si hay usuario Firebase activo pero falta token local, `MainActivity` refresca el token, lo guarda y abre `HomeActivity`.
- Si no hay usuario Firebase activo, `MainActivity` borra la sesion local.

Logout:

- `HomeActivity` ahora hace `FirebaseAuth.signOut()`.
- Borra `LocalSession`.
- Abre `MainActivity` con `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`.
- Ya no usa `finish()` como accion principal, asi que vuelve a la pantalla inicial en vez de cerrar la app.

### 2026-06-01 - Primera pantalla real de partida Android

Se empezo a convertir `GameFragment` en una pantalla de partida usable:

- `BackendClient` ahora tiene `getGame(...)`.
- `BackendClient` ahora tiene `selectCreatures(...)`.
- `activity_game.xml` incluye `gameBoardContainer`.
- `GameFragment` carga el estado de partida desde `GET /api/games/{gameId}`.
- En fase `SELECTING_CREATURES`, permite seleccionar:
  - 2 criaturas activas.
  - 1 criatura oculta.
- Envia la seleccion a `POST /api/games/{gameId}/creatures`.
- Si el jugador ya envio criaturas, muestra estado de espera del rival.
- Mientras espera al rival, refresca la partida cada 3 segundos.
- Cuando la partida pasa a `IN_PROGRESS`, muestra un tablero basico:
  - Turno actual.
  - Criaturas del rival.
  - Criaturas propias.
  - Estado, posicion y vida actual de cada criatura.
- El tablero se refresca cada 3 segundos mientras la partida esta en curso.

Nota:

- De momento el tablero muestra una primera representacion funcional, no el diseno final de combate.

### 2026-06-01 - Seleccion de criaturas respeta copias duplicadas

Se corrigio un problema en `GameFragment`:

- Antes las criaturas seleccionables se guardaban en un `Set<String>` por `cardId`.
- Si un deck tenia varias copias de la misma criatura, solo aparecia una vez.
- Eso podia impedir seleccionar 2 activas y 1 oculta cuando las 3 criaturas eran copias del mismo modelo.

Cambio:

- Ahora se crea una lista `deckCreatureChoices`.
- Cada copia tiene un `choiceId` interno diferente, por ejemplo `card_id#1`, `card_id#2`.
- La UI muestra cada copia como opcion independiente.
- Al enviar al backend, se convierte cada `choiceId` al `cardId` real.
- Esto permite enviar duplicados en `activeCreatureCardIds` y usar otra copia como `hiddenCreatureCardId`.

### 2026-06-01 - Partida Android fuera del contenedor con footer

Se cambio la navegacion al abrir una partida desde el home:

- `HomeActivity.showGame(gameId)` ya no reemplaza el contenido central con `GameFragment`.
- Ahora abre `GameActivity` con el `gameId` en el intent.
- `GameActivity` monta `GameFragment` sobre `android.R.id.content`, usando toda la pantalla.
- Al quedar fuera de `activity_home.xml`, la partida ya no muestra la barra inferior/footer.
- `GameFragment` mantiene compatibilidad: si esta dentro de `HomeActivity`, `Volver` regresa al menu; si esta dentro de `GameActivity`, `Volver` cierra la pantalla de partida.
- `activity_game.xml` tiene ahora fondo `@drawable/background_2` y padding propio porque ya no hereda los margenes visuales del contenedor del home.

### 2026-06-01 - Backend publica URL temporal de Cloudflare Tunnel

Se preparo el backend para pruebas con Cloudflare Tunnel temporal:

- Se creo `CloudflareTunnelPublisher`.
- Al arrancar Spring Boot, si `cloudflare.tunnel.enabled=true`, lanza:

```text
cloudflared tunnel --url http://localhost:8080
```

- El componente lee la URL `https://*.trycloudflare.com` que imprime `cloudflared`.
- Guarda la configuracion publica en Firebase Realtime Database:

```text
config/backend
```

Con forma:

```json
{
  "baseUrl": "https://xxxx.trycloudflare.com",
  "generatedAt": "2026-06-01T...",
  "source": "cloudflare-tunnel",
  "localUrl": "http://localhost:8080"
}
```

Propiedades anadidas en `server/src/main/resources/application.properties`:

```properties
cloudflare.tunnel.enabled=true
cloudflare.tunnel.command=cloudflared
cloudflare.tunnel.local-url=http://localhost:${server.port}
cloudflare.tunnel.firebase-path=config/backend
```

Notas:

- Requiere tener `cloudflared` instalado y accesible en el PATH.
- Si `cloudflared` no esta instalado, el backend no se cae; solo registra aviso en logs.
- Android deberia leer `config/backend/baseUrl` desde Firebase antes de crear `BackendClient`.

En Windows, `cloudflared` instalado con Winget no quedo disponible en el PATH usado por Spring Boot. Se cambio `application.properties` para apuntar a la ruta exacta:

```properties
cloudflare.tunnel.command=C:/Users/Alejandro Rasero/AppData/Local/Microsoft/WinGet/Packages/Cloudflare.cloudflared_Microsoft.Winget.Source_8wekyb3d8bbwe/cloudflared.exe
```

### 2026-06-01 - Android consume URL de backend desde Firebase

Se adapto Android para usar la URL temporal publicada por el backend:

- Se creo `BackendConfig`.
- Al arrancar `MainActivity`, la app lee:

```text
config/backend/baseUrl
```

- Si existe, guarda la URL en `SharedPreferences`.
- `BackendClient` ya no fija la URL al crearse; en cada peticion usa `BackendConfig.getBaseUrl(...)`.
- Si Firebase no devuelve URL, mantiene como fallback `backend_base_url` en `strings.xml`.
- `LoginActivity` refresca la URL antes de abrir `HomeActivity`.
- `RegisterActivity` refresca la URL antes de llamar a `POST /api/users`, para crear el perfil contra el tunel actual.
- `LocalSession.clear()` ahora borra solo el token local y conserva `backend_base_url`.
- El backend imprime en consola:

```text
Cloudflare Tunnel URL generada: https://xxxx.trycloudflare.com
```

### 2026-06-05 - Logica real de turnos

Se implemento la logica completa del ciclo de turnos.

Ultima actualizacion: 2026-06-05

#### Modelos ampliados

`GamePlayerState` ahora tiene:
- `actionDeck: List<String>` — cartas de accion no robadas aun
- `hand: List<String>` — mano actual del jugador (max 5)
- `discardPile: List<String>` — cartas descartadas

`FieldCard` ahora tiene:
- `maxHealth: int` — vida maxima de la criatura
- `attack: int` — ataque de la criatura
- `speed: int` — velocidad (determina orden de resolucion)

Estos campos se rellenan al crear el `FieldCard` desde el catalogo global de cartas.

#### Repositorio: getDeckActionCardIds

`UserProfileRepository` tiene un nuevo metodo:

```java
getDeckActionCardIds(uid, deckId, catalog) -> CompletableFuture<List<String>>
```

Lee `users/{uid}/decks/{deckId}`, filtra las cartas de tipo `ACTION` y las expande por `quantity`. Devuelve una lista plana de IDs lista para barajar.

#### Cambios en GameService

Se inyectaron `CardRepository` y `UserProfileRepository`.

`createGame()`:
- Ahora es async real.
- Carga el catalogo de cartas y los mazos de accion de ambos jugadores.
- Baraja cada mazo y lo guarda en `GamePlayerState.actionDeck`.

`selectCreatures()`:
- Cuando ambos jugadores han seleccionado, carga el catalogo y llama a `startFirstTurn()`.

`startFirstTurn()`:
- Inicializa `currentHealth`, `maxHealth`, `attack` y `speed` en cada `FieldCard` desde el catalogo.
- Llama a `drawCards()` para ambos jugadores (5 cartas de inicio).

`drawCards(state, count)`:
- Si el mazo de accion no tiene suficientes cartas, baraja el descarte y lo anade al mazo antes de robar.
- Nunca se agotan las cartas.

`submitTurnSelection()`:
- Valida que cada carta de accion este en la mano del jugador.
- Valida que la criatura origen pertenezca al jugador y este ACTIVE.
- Valida que la criatura objetivo exista en el campo.
- Fija `move.speed` desde la velocidad del `FieldCard` origen.
- Cuando ambos jugadores han enviado acciones, carga el catalogo y resuelve el turno.
- Si la partida termina, saca el juego del `ActiveGameStore` y lo guarda en Firebase.
- Si la partida continua, llama a `startNextTurn()`.

`resolveTurn(turn, game, catalog)`:
- Ordena los movimientos por `speed` descendente.
- Aplica el dano del efecto de cada carta de accion al `FieldCard` objetivo.
- Los curaciones se limitan a `maxHealth`.
- Si `currentHealth <= 0`: el `FieldCard` pasa a `DEAD` y se incrementa `deadCreatures` del dueno.
- Si algun jugador llega a 3 criaturas muertas, la partida pasa a `FINISHED` con ganador.
- Devuelve `true` si la partida termino, `false` si continua.

`startNextTurn()`:
- Descarta mano restante y cartas jugadas de ambos jugadores a `discardPile`.
- Roba 5 cartas nuevas para cada jugador.
- Crea un nuevo `Turn` con el siguiente numero y nuevos plazos.
- Copia las `FieldCard` no DEAD del turno anterior al nuevo turno (preservando `currentHealth`).

#### Cambios en Android

`BackendClient` tiene nuevo metodo `submitTurnSelection(idToken, gameId, body, callback)` que llama a `POST /api/games/{gameId}/turn-selection`.

`GameFragment` ampliado para la fase `IN_PROGRESS`:
- Al entrar en `IN_PROGRESS`, carga todas las cartas del catalogo desde Firebase (una sola vez).
- Lee la mano del jugador desde `game.players[uid].hand` en el JSON de la partida.
- Muestra la seccion "Tu mano" con un panel por cada carta.
- Flujo de seleccion de accion en tres pasos:
  1. Pulsar "Jugar" en una carta de mano.
  2. Elegir criatura propia activa como origen.
  3. Elegir criatura objetivo (cualquier no DEAD).
- Acciones preparadas se muestran en una lista con boton "X" para cancelar cada una.
- Boton "Confirmar turno (N acciones)" envia a `POST /api/games/{gameId}/turn-selection`.
- Tras confirmar, muestra "Turno enviado. Esperando al rival..." hasta que llegue el siguiente turno.
- Las cartas muertas se muestran en gris con vida 0.
- La vida se muestra como "actual/maxima" cuando `maxHealth > 0`.
- Al detectar `FINISHED`, muestra "Has ganado" o "Has perdido" segun `winnerId`.

### 2026-06-05 - Correcciones seleccion de acciones en partida

Dos bugs corregidos en `GameFragment`:

1. **Cartas duplicadas en mano marcadas como staged incorrectamente.**
   - Causa: la comprobacion `alreadyStaged` usaba `cardId` para identificar la accion, lo que marcaba todas las copias del mismo tipo.
   - Solucion: `StagedAction` ahora guarda `handIndex` (posicion en la mano). La comprobacion `alreadyStaged` y `isBeingStaged` usan el indice, no el `cardId`.

2. **Seleccion de objetivo mostraba criaturas en banca.**
   - Causa: el filtro usaba `!"DEAD"` como condicion, mostrando tambien las criaturas con `status=BENCH`.
   - Solucion: el filtro de objetivos ahora exige `"ACTIVE"` explicitamente. Las criaturas ocultas en reserva no son objetivos validos.

### 2026-06-05 - Reglas de combate corregidas

Tres correcciones de reglas:

1. **Solo se puede atacar criaturas activas del rival (Android).**
   - El paso 2 de seleccion de objetivo ahora itera unicamente `boardOpponentCards` con `status=ACTIVE`.
   - Las criaturas propias no aparecen como objetivo (antes si aparecian junto a las rivales).

2. **Cada criatura activa solo puede ser fuente de una accion por turno (Android).**
   - En el paso 1 de seleccion de origen se recogen los `sourceFieldCardId` ya usados en acciones preparadas.
   - Las criaturas ya usadas como fuente quedan excluidas de la lista de seleccion.
   - Resultado: con 2 acciones por turno y 2 criaturas activas, cada criatura hace exactamente una accion.

3. **La criatura en banca sale automaticamente al campo al morir una activa (Backend).**
   - `GameService.resolveTurn()` llama a `promoteBenchCreature(turn, ownerId)` inmediatamente despues de marcar una criatura como DEAD.
   - `promoteBenchCreature` busca el `FieldCard` con `status=BENCH` del mismo jugador y lo cambia a `status=ACTIVE` y `visibility=PUBLIC`.
   - La nueva criatura activa puede ser objetivo de acciones posteriores dentro del mismo turno de resolucion.

### 2026-06-05 - Rediseno de pantalla de Perfil estilo videojuego

Se rediseno completamente la pantalla de perfil para darle estetica de videojuego TCG.

**Nuevos drawables:**
- `stat_badge_background.xml`: panel oscuro (#CC0D1520) con borde dorado (#88C9A84C) y esquinas redondeadas 10dp.
- `avatar_background.xml`: forma oval oscura con borde dorado 2dp — forma el avatar circular del jugador.
- `profile_header_background.xml`: gradiente oscuro (navy → muy oscuro) con borde dorado sutil.
- `divider_gold.xml`: linea separadora dorada semitransparente 1dp de alto.

**Nuevo `activity_profile.xml`:**
- Header con avatar circular (letra inicial del username, dorado sobre fondo oval).
- Nombre de jugador (22sp bold) y email (pequeno, muted).
- Fila de 3 estadisticas en paneles iguales con borde dorado: MONEDAS, PUNTOS (MMR), RANGO (ELO).
- Panel de MAZO ACTIVO con separador dorado.
- Boton SINCRONIZAR en dorado (#C9A84C) y boton VOLVER outline.

**Nuevo `ProfileFragment.java`:**
- IDs actualizados: `avatarText`, `usernameText`, `emailText`, `coinsValue`, `mmrValue`, `eloValue`, `deckInfoText`.
- Se eliminaron los bloques de texto plano `firebaseInfoText` y `backendInfoText`.
- Cada campo de stat se actualiza individualmente.
- `avatarText` muestra la primera letra del username en mayuscula.
- Logica de Firebase realtime listener sin cambios.

### 2026-06-05 - Mano en barra inferior y seleccion visual de criaturas

Se rediseno la UI de partida para separar la mano del tablero y hacer la seleccion de accion mas intuitiva.

**Cambios en `activity_game.xml`:**
- Layout raiz cambiado de `ScrollView` a `LinearLayout` vertical.
- `boardScrollView` nuevo: ocupa toda la zona central (weight=1) durante `IN_PROGRESS`.
- `creatureSelectionScrollView` nuevo: ocupa la misma zona durante `SELECTING_CREATURES`. Cuando uno es VISIBLE el otro es GONE.
- `handSection` nuevo: barra fija en la parte inferior, contiene `promptText` y `handContainer` (HorizontalScrollView).
- `submitCreaturesButton` sigue visible solo en `SELECTING_CREATURES`.

**Cambios en `GameFragment.java`:**
- Nuevos campos: `handContainer`, `promptText`, `handSection`, `boardScrollView`, `creatureSelectionScrollView`.
- Se eliminaron `renderTurnActions()` y `renderHandCard()`.
- Nuevo `renderHand(JSONArray hand)`: popula `handContainer` con 5 slots fijos de cartas. Muestra el prompt de seleccion sobre la mano.
- Nuevo `createHandCardView(cardId, handIndex)`: slot de carta con nombre y efecto. Click inicia seleccion; click de nuevo cancela. Si hay accion pendiente, las otras cartas se deshabilitan (alpha 0.45).
- Nuevo `createEmptyHandSlot()`: slot vacio sin carta.
- `createFieldCardView(fieldCard, isMySection)` ahora devuelve `View` (antes `TextView`). Segun el paso de seleccion:
  - **Paso origen** (pendingHandIndex != -1, pendingSourceFieldCardId == null): mis criaturas ACTIVE disponibles se iluminan dorado y son clickables.
  - **Paso objetivo** (pendingSourceFieldCardId != null): criaturas ACTIVE del rival se iluminan rojo y son clickables. Click registra la accion staged.
  - Criaturas no aplicables se atenuan (alpha 0.4).
  - Criatura origen seleccionada se muestra en azul.
- `promptText` muestra:
  - "¿Que criatura hara la accion?" en paso origen.
  - "¿A que objetivo hara la accion?" en paso objetivo.
  - Oculto cuando no hay seleccion pendiente.
- `addBoardSection` recibe nuevo parametro `boolean isMySection`.
- Gestion de visibilidad de secciones centralizada en `renderGame()` segun estado.
