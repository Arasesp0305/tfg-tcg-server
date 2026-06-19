# Memoria del proyecto TFG TCG

Ultima actualizacion: 2026-06-19

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

### 2026-06-07 - Listado de cartas (coleccion) en 3 columnas con formato de mano

`CollectionFragment.java`: `renderCollection()` ahora reparte las cartas en filas de `CARDS_PER_ROW = 3` (LinearLayout horizontal con `weightSum`), usando filler invisible para completar la ultima fila incompleta. `createCardView()` reescrito para usar el mismo formato visual que las cartas de la mano en partida (`createHandCardView` en `GameFragment`): panel vertical con `info_panel_background`, padding 12, nombre en negrita blanco tamano 12 ("Nombre xCantidad") y descripcion (tipo + texto) en gris tamano 10. Nuevo helper `createCardRow()` / `createRowFiller()`.

### 2026-06-07 (2) - Cartas de coleccion: tamano fijo, proporcion real, texto truncado con "..."

`CollectionFragment.java`: `createCardView()` ahora envuelve el panel en un `FrameLayout` de tamano fijo `CARD_WIDTH_DP=100` x `CARD_HEIGHT_DP=140` (ratio 5:7, proporcion de carta de coleccionable real ~63x88mm), centrado dentro de la celda de la fila (weight=1) -> todas las cartas miden igual independientemente del texto. Nombre con `maxLines(2)` y descripcion con `maxLines(6)`, ambos `setEllipsize(TextUtils.TruncateAt.END)`: si el texto no cabe se corta y anade "...". Nuevo helper `dpToPx()`.

### 2026-06-07 (3) - Fix: error 400 "No existe perfil de usuario" al comprar sobre la primera vez

**Causa:** `UserProfileRepository.registerPurchase()` lanzaba `runTransaction()` directamente sobre `users/{uid}`. La primera vez que esa ruta se usa, la cache local del SDK Firebase aun no esta sincronizada y `doTransaction` recibe `currentData.getValue() == null` aunque el perfil exista en el servidor -> el codigo lo trataba como "perfil inexistente" y abortaba con 400. La segunda pulsacion ya funcionaba porque la cache quedaba poblada tras el primer intento.

**Fix:** antes de iniciar la transaccion, se hace una lectura previa con `findById(uid)` (pobla la cache via listener single-value) y solo si el perfil de verdad no existe se falla con `IllegalArgumentException`. La transaccion (renombrada a `runPurchaseTransaction`) ya recibe `currentData` sincronizado en el primer intento.

### 2026-06-07 (4) - Decision de tipografia/tamanos/colores global (coherente con paneles navy + dorado)

Se formalizo un sistema tipografico+color en `colors.xml`/`themes.xml` basado en la paleta ya presente (navy #101828/#172033/#0D1520, dorado #C9A84C, blanco translucido):
- **Fuentes** (familias del sistema, sin assets nuevos): `serif` (Noto Serif, negrita, dorado) para titulos/cabeceras/nombres de carta -> look "grabado" acorde a la estetica TCG; `sans-serif-condensed` para cuerpo/captions/botones -> compacto y legible en paneles pequenos.
- **Tamanos**: titulo pantalla 28sp, cabecera seccion 18sp, cuerpo 15sp, captions/stats 12sp, boton 16sp.
- **Colores nuevos** en `colors.xml`: `navy_deepest/navy_dark/navy`, `gold`, `ink/ink_soft/ink_muted`.
- `themes.xml`: defino `TextAppearance.TFGGame.{Title,Heading,Body,Caption}` y `Widget.TFGGame.Button` (dorado sobre navy_deepest, condensed medium, sin mayusculas forzadas), enlazados como defaults del Material3 theme (`textAppearanceHeadlineMedium`, `materialButtonStyle`, etc) -> toda pantalla nueva hereda el estilo sin tocarla.
- Se elimino `values-night/themes.xml` (la app es oscura por diseno; sin esa variante, Android usa siempre `values/themes.xml` en modo dia y noche).

### 2026-06-07 (5) - Fix: tipografia se veia "blanca, igual en todo, estirada verticalmente"

**Causa:** el `android:fontFamily="sans-serif-condensed"` puesto en `Base.Theme.TFGGame` se filtraba a TODOS los TextView/Button (la app construye casi toda su UI con colores/tamanos fijos en XML o `setTextColor`/`setTypeface` en codigo, que no tocan `fontFamily`). Resultado: todo el texto paso a usar la misma fuente condensada (glifos mas estrechos = look "estirado verticalmente"), mientras que el color seguia siendo el blanco hardcodeado de siempre -> el cambio de tipografia/color "no se veia" donde se queria (titulos) y SI se notaba donde no se queria (todo lo demas).

**Fix:** se quito `android:fontFamily` y los mapeos `textAppearance*`/`materialButtonStyle` del theme base (no llegan a vistas con estilo explicito de todos modos). Las decisiones de [[fuente/color/tamano]] se aplican ahora elemento a elemento donde corresponde: los 6 titulos de pantalla (`activity_collection`, `activity_shop`, `fragment_home_menu`, `activity_login`, `activity_register`, `activity_main`) pasan a `android:fontFamily="serif"` + `android:textColor="@color/gold"`; `subtitleText` de `activity_main` a `sans-serif-condensed` + `@color/ink_soft`. Los estilos `TextAppearance.TFGGame.*`/`Widget.TFGGame.Button` quedan definidos en `themes.xml` para referenciarlos explicitamente donde se necesite, pero ya no se enlazan globalmente.

### 2026-06-07 (6) - Logo, splash screen y barras de texto con assets nuevos (logo.png, textbar.png)

**Icono de escritorio:** `AndroidManifest.xml` `<application>` -> `android:icon`/`android:roundIcon` cambiados de `@mipmap/ic_launcher*` a `@drawable/logo` (PNG circular "Chronicle Realms"). Se omite el sistema de adaptive icon (mipmap-anydpi/ic_launcher.xml) porque el logo ya trae su propio fondo circular.

**Pantalla de carga:** nuevo drawable `splash_screen.xml` (layer-list: `background_2` + `logo` 220dp centrado) y estilo `Theme.TFGGame.Splash` (extiende `Theme.TFGGame`, solo cambia `android:windowBackground`). `MainActivity` declarada en el manifest con ese theme -> se ve desde el instante de arranque (antes de inflar nada) y permanece visible durante `FirebaseInitializer.initialize` + `BackendConfig.refresh` porque es simplemente el windowBackground de la Activity hasta que `continueStartup()` llama a `setContentView`. No hizo falta tocar `MainActivity.java`.

**Cuadros de texto (login/register):** `emailInput`/`passwordInput` en `activity_login.xml` y `activity_register.xml`: `android:background` cambiado de `@drawable/auth_input_background` (placa blanca) a `@drawable/textbar` (plantilla con marco dorado), + `paddingHorizontal=22dp` para no pisar el marco ornamentado, texto escrito en `@color/gold` con `sans-serif-condensed` (decision tipografica de [[2026-06-07 (4)]]) y hint en `@color/ink_muted`. `auth_input_background` se mantiene (sigue usandose en `activity_deck_builder.xml`).

### 2026-06-07 (7) - Ajustes: icono transparente, splash sin logo-estirado, barras menos finas

**Logo transparente en escritorio:** revertido `android:icon`/`android:roundIcon` a `@mipmap/ic_launcher*` (adaptive icon) -- usar `@drawable/logo` crudo como icono de app rompia el splash de Android 12+ (ver abajo). Nuevo `ic_launcher_logo_foreground.xml` (`<inset>` del logo al 22% por lado, para caer en la zona segura ~66% del adaptive icon) y `mipmap-anydpi/ic_launcher{,_round}.xml` ahora usan `background = @android:color/transparent` + ese foreground -> el logo se ve sin caja/fondo solido detras, solo su propio circulo.

**Splash "logo de fondo" -> causa real:** en Android 12+ el sistema SIEMPRE pinta su propio splash al arrancar (no se puede sustituir por un drawable cualquiera; su `windowSplashScreenBackground` solo admite color solido, nunca una imagen) y por defecto usa el `android:icon` de la app. Al haberlo puesto a `logo.png` crudo (1254x1254, sin recorte de zona segura), el sistema lo escalaba/estiraba a pantalla completa -> parecia "el logo es el fondo". Fix: `Theme.TFGGame.Splash` ahora define explicitamente `windowSplashScreenBackground = @color/navy_deepest` y `windowSplashScreenAnimatedIcon = @drawable/ic_launcher_logo_foreground` (mismo logo recortado del icono) con `tools:targetApi="31"`. Justo despues entra el `windowBackground` normal de la Activity (`splash_screen.xml`: `background_2` + logo centrado 220dp), que es la pantalla de carga "real" pedida y dura mientras `FirebaseInitializer`+`BackendConfig.refresh` cargan.

**Barras de texto "demasiado finas":** `textbar.png` es ~2:1 (1774x887); los `EditText` median 56dp de alto sobre ~300dp de ancho (~5.4:1) -> se veian aplastadas verticalmente. Subido `android:layout_height` de los 4 campos (`emailInput`/`passwordInput` en login y register) de `56dp` a `80dp`.

### 2026-06-07 (8) - Textbar aun comprimido -> alto x2 (160dp)

Usuario pidio doblar el alto de los 4 campos (`emailInput`/`passwordInput` login+register): `80dp` -> `160dp`. Vigilar en pantallas pequenas: 2 campos x160dp + titulo + boton + status podrian no caber sin overlap (no se ajustaron margenes).

### 2026-06-07 (9) - Texto del textbar pegado al adorno izquierdo -> mas padding-start

`paddingHorizontal=22dp` -> `paddingStart=40dp`/`paddingEnd=22dp` en los 4 campos (login+register, email+password): el texto arrancaba sobre el adorno dorado de la esquina izquierda del marco; ahora entra completo dentro de la placa.

### 2026-06-07 (10) - Pantalla de decks: separa listado vs edicion, grid de cartas estilo "Cartas" con +/- y badge "x?"

**Pedido:** la pantalla de decks debia mostrar SOLO un listado (cada deck con boton "Editar" + boton "Nuevo deck" aparte); al editar, mostrar las cartas en el mismo formato que "Cartas" (grid 3 columnas, tamano fijo 100x140dp, proporcion real 5:7, texto truncado con elipsis), con un badge "x?" en la esquina superior = cantidad de esa carta que queda sin meter en el deck, y controles "+"/"-" por carta.

**Layout (`activity_deck_builder.xml`):** dividido en `deckListSection` (boton `newDeckButton` + `savedDecksContainer`, visible por defecto) y `deckEditSection` (`deckNameInput` + `deckStatusText` + `deckCardsContainer` + `saveDeckButton` + `cancelEditButton`, `visibility="gone"` por defecto). `deckBackButton` queda fuera de ambas secciones (siempre visible, vuelve al menu). Se quito el antiguo titulo "Crear deck"/`deckStatusText` fijo en cabecera; titulo estatico ahora "Tus decks".

**`DeckBuilderFragment.java`:** nuevos `showDeckList()`/`showDeckEditor()` togglean `View.GONE`/`VISIBLE` entre secciones (refactor de [[2026-06-07]] del fragmento que ya combinaba listado+editor en una sola vista). `startNewDeck()`/`loadDeckForEditing()` ahora llaman `showDeckEditor()`; `cancelEditButton` -> `showDeckList()` (limpia `editingDeckId`/`selectedCards`, descarta cambios no guardados -- comportamiento igual al de cambiar de deck sin guardar, que ya sobrescribia `selectedCards`).

**Grid de cartas reutiliza el formato de [[CollectionFragment]]** (`CARDS_PER_ROW=3`, `CARD_WIDTH_DP=100`/`CARD_HEIGHT_DP=140`, `info_panel_background`, nombre+tipo/texto truncados con `TextUtils.TruncateAt.END`): nuevo `createCardCell(cardId)` envuelve el panel de carta en un `FrameLayout` para superponer el badge "x{remaining}" (= `ownedQuantity - selectedQuantity`, fondo `stat_badge_background`, texto `@color/gold` 0xFFC9A84C en esquina superior-derecha vía `Gravity.TOP|END`), y debajo anade fila de acciones reutilizando la logica YA EXISTENTE `changeSelectedQuantity`/`selectedCards` (antes en `createCardRow`, ahora eliminado en favor del grid).

### 2026-06-07 (11) - "Cartas" (CollectionFragment): badge "x{cantidad}" tambien en esquina superior-derecha

Mismo tratamiento visual que el badge "x{remaining}" de [[2026-06-07 (10)]] en el editor de decks: `createCardView` ya no concatena " x{quantity}" al nombre (texto truncable), ahora lo muestra en `quantityBadge` (`stat_badge_background`, texto `@color/gold` 0xFFC9A84C) superpuesto via `FrameLayout` (`cardFrame` de tamano fijo 100x140dp) en esquina superior-derecha (`Gravity.TOP|END`). Estructura: `cell` (weight=1) -> `cardFrame` (tamano fijo, centrado) -> `slot` (panel info) + `quantityBadge` (overlay).

### 2026-06-07 (12) - /frontend-design aplicado a "Tus decks": linea "scriptorium/ledger" dorado-sobre-navy

**Direccion elegida:** tratar el deck como un tomo/sello del jugador -- titulo serif dorado + filete `divider_gold` bajo el, primera vez que se usan en produccion los estilos `Widget.TFGGame.Button` (definidos sin enlazar desde [[2026-06-07 (5)]]) en `newDeckButton`/`saveDeckButton` (gold solido, texto navy, sin mayusculas). `saveDeckButton` renombrado "Sellar deck" (refuerza metafora "sello de cera" del TCG de fantasia).

**Filas de deck (`createSavedDeckRow`):** nombre en serif-bold dorado (trunca a 1 linea), chip de estado reutilizando `stat_badge_background` (mismo lenguaje visual que los badges "x?" de [[2026-06-07 (10)]]/[[2026-06-07 (11)]]) -- "Listo para combate" en dorado si `playable`, "Incompleto" en `ink_muted` si no; boton "Editar deck" repintado de blanco/navy a dorado solido + texto navy_deepest (consistente con `Widget.TFGGame.Button`, sin crear estilo nuevo via `setBackgroundTintList`).

**Resto:** `deckNameInput` recibe `sans-serif-condensed` (antes sin fontFamily); `deckStatusText` pasa de blanco puro a `@color/ink_soft` + condensed + `lineSpacingExtra`; texto de "sin decks" reescrito con tono tematico ("Aun no has forjado ningun deck...") en italica translucida.

### 2026-06-07 (13) - /frontend-design: tarjetas de "Cartas" y "Editar deck" -- placa "grabada" (engraved plaque)

**Bug de consistencia detectado y corregido:** `themes.xml` (decision de [[2026-06-07 (5)]]) documenta "nombres de carta -> serif, negrita, dorado: look grabado", pero `createCardView`/`createCardCell` pintaban el nombre en sans bold blanco (0xFFFFFFFF). Unificado: nombre ahora `Typeface.SERIF` bold + `@color/gold` (0xFFC9A84C) en AMBAS vistas (`CollectionFragment` y `DeckBuilderFragment`).

**Nuevo elemento "engravingRule":** filete `divider_gold` de 34dp entre nombre y cuerpo -- separa visualmente titulo "grabado" del texto, refuerza metafora placa/sello ya usada en [[2026-06-07 (12)]].

**Cuerpo de texto:** tipo de carta pasa a mayusculas (`type.toUpperCase(Locale.ROOT)`) como "etiqueta estampada" sobre una linea propia + texto debajo; color unificado a `0x99FFFFFF` (mismo valor que `ink_muted`, antes `0x88FFFFFF`/inconsistente entre fragments), `sans-serif` explicito + `letterSpacing=0.01` para look "tipografia de ficha". `maxLines` ajustado 6->5 para dejar hueco al filete sin desbordar el panel fijo 100x140dp.

Mismo cambio aplicado identico en los dos fragments (duplican estructura de carta, ya documentado en [[2026-06-07 (10)]]) para que "Cartas" y "Editar deck" luzcan idénticas como pidio el usuario originalmente.

### 2026-06-07 (14) - /frontend-design en Tienda: "El Mercader Errante" -- mismo lenguaje sello/grabado

**Titulo rebautizado** "Tienda" -> "El Mercader Errante" (serif/gold + `divider_gold` debajo, igual que [[2026-06-07 (12)]]); `shopStatusText` pasa de blanco/sans a condensed italic `ink_muted`.

**`createPackView` (filas de sobre):** cabecera horizontal nombre (serif-bold gold, 1 linea, ellipsize) + chip de precio "🪙 {N}" reutilizando `stat_badge_background` (mismo lenguaje que chips de [[2026-06-07 (12)]]/[[2026-06-07 (13)]]); filete `engravingRule` (`divider_gold`, 40dp) separa cabecera de cuerpo; descripcion en `sans-serif` + `ink_soft`; nueva etiqueta "CONTIENE N CARTAS" estilo "estampado" (mayusculas, `letterSpacing=0.12`, `ink_muted`, 10sp) sustituye el antiguo "Cartas por sobre: N" inline. Boton "Comprar y abrir" -> "Romper el sello" (metafora sello de cera/TCG), repintado gold-solido/navy-texto via `setBackgroundTintList` (mismo patron que "Editar deck"/"Sellar deck").

**`renderOpenResult`:** texto generico "Cartas obtenidas:" -> "El sello se rompe y aparecen:", bullets "-" -> "✦" (refuerza tema mistico/TCG); `packResultText` (XML) pasa de blanco puro a `ink_soft` + condensed + `lineSpacingExtra`.

Nuevo helper `dpToPx` anadido a `ShopFragment` (mismo patron que `CollectionFragment`/`DeckBuilderFragment`).

### 2026-06-07 (15) - /frontend-design en menu principal: titulo "Chronicle Realms", chip de monedas estilo sello, boton dorado

**`fragment_home_menu.xml`:**
- Titulo "Menu principal" -> "Chronicle Realms" (nombre del juego, refuerza identidad; serif/gold ya estaba). Insertado `menuDivider` (`divider_gold`, 120dp) en la cadena vertical entre titulo y `userText` (re-cablear constraints: titulo->divider->userText->boton, `chainStyle=packed` se mantiene en el primer elemento) -- mismo gesto que [[2026-06-07 (12)]]/[[2026-06-07 (14)]].
- `coinsContainer`: `coin_balance_background` (pildora blanca solida, desentonaba con la paleta navy/dorado ya extendida a deck/cartas/tienda) -> `stat_badge_background` (navy translucido + borde dorado), `coinsText` pasa de navy-on-white a `@color/gold` + `sans-serif-condensed-medium`. Mismo "chip de sello" que precio de sobres en [[2026-06-07 (14)]].
- `userText` (email del jugador): blanco/sans -> `ink_muted` + `sans-serif-condensed` italic, tono "firma de escriba".
- `matchmakingButton`: primer uso de `style="@style/Widget.TFGGame.Button"` via XML directo (antes solo via `setBackgroundTintList` programatico en [[2026-06-07 (12)]]/[[2026-06-07 (14)]]); renombrado "Buscar partida" -> "Buscar duelo" (tono TCG).

No se toco `HomeMenuFragment.java` (solo fija textos/listeners, sin construir vistas).

### 2026-06-07 (16) - /frontend-design en tablero de partida: tipografia/cromos del scriptorium SIN tocar codigos de color de estado

**Cuidado especial:** `GameFragment` (1081 lineas) codifica estado de juego mediante color (gold=#E9D8A6 "puedes actuar", azul=#6699FF "origen seleccionado", rojo=#FF5555 "objetivo enemigo", gris=#888888 "muerta", navy=`info_panel_background` neutral). Cambiar esos valores podria romper la legibilidad funcional durante una partida real (no se puede playtest interactivo aqui) -> se dejaron intactos. Solo se toco TIPOGRAFIA (independiente del color-estado) y el CHROME estatico (no ligado a estado de partida).

**Tipografia de nombres de carta** (`createHandCardView`/`createFieldCardView`): sans bold -> `Typeface.SERIF` bold, mismo "look grabado" de [[2026-06-07 (13)]], conservando exactamente la logica de color condicional existente (seleccion/preparada/muerta/etc. intacta).

**Encabezados de tablero** (`addBoardTitle`: "Tu turno"/"Zona rival"/"Acciones preparadas"/etc.): blanco bold -> serif bold dorado (`0xFFC9A84C`) + `letterSpacing=0.02`, igual que `TextAppearance.TFGGame.Heading`. **Texto informativo** (`addBoardText`: "Sin cartas"/"Esperando turno..."): blanco -> italica `sans-serif` `0x99FFFFFF` (mismo tono "nota de manuscrito" que vacios de [[2026-06-07 (12)]]).

**Chrome estatico (`activity_game.xml`):** filete `divider_gold` bajo la barra superior y sobre la seccion de mano (separa tablero/mano como un folio); `gameStatusText` -> condensed `ink_muted`; `gameBackButton` -> condensed `ink_soft`; `promptText` ("¿Que criatura hara la accion?") -> `fontFamily=serif` (tono "proclama del heraldo", color gold de aviso intacto); `submitCreaturesButton` -> `Widget.TFGGame.Button` (gold solido, mismo patron que [[2026-06-07 (15)]]).

(17) [2026-06-07] Tablero de partida — separacion activas/banca por filas
Cambio: `addBoardSection` (GameFragment.java) ahora separa fieldCards por `status` ("ACTIVE" vs resto = banca) y renderiza dos filas via nuevo helper `addFieldCardRow(list, isMySection, isBench)`. Orden vertical:
- Mis criaturas: fila activas arriba, banca debajo (alpha 0.6, cartas 96dp)
- Rival: banca arriba (alpha 0.6), activas abajo, pegadas a mi linea — refleja layout fisico tablero (lineas activas enfrentadas, bancas detras)
`boardCardParams(isBench)` ahora devuelve ancho fijo (140dp activas / 96dp banca) en vez de weight=1, asi 2 activas quedan lado a lado centradas sin estirarse a todo el ancho.
[[chronicle-realms-scriptorium-aesthetic]] — colores de estado (gold/blue/red/gray) en createFieldCardView intactos, solo se toco estructura de filas/tamanos.
Build: `./gradlew :app:compileDebugJavaWithJavac -q` limpio.

### 2026-06-11 - Replay de resolucion de turno (1 accion/segundo)

`GameFragment.java`: al detectar cambio de `currentTurnId` (o partida `FINISHED`), si el turno previo tiene `resolvedMoves` no mostrados (`lastResolvedTurnId`), se reproduce antes de pintar el tablero nuevo:
- `handleBoardUpdate(game)` (sustituye llamada directa a `renderBoard` en IN_PROGRESS): calcula `prevTurnId = turn_%03d(number-1)`, si `turns[prevTurnId].resolvedMoves` no vacio y distinto de `lastResolvedTurnId` -> `stopGameRefresh()` + `playMoveReplay(prevTurn, ...)`; al terminar marca `lastResolvedTurnId`, llama `renderBoard(game)` y reanuda `scheduleGameRefresh()`.
- `handleFinishedGame(game)` (sustituye bloque inline en FINISHED): mismo chequeo sobre `turns[currentTurnId].resolvedMoves` (turno que acabo la partida) antes de mostrar "Has ganado/perdido".
- Nuevo campo `lastResolvedTurnId` (turnId del ultimo turno cuyas `resolvedMoves` ya se reprodujeron), evita repetir replay en cada poll de 3s.

**(actualizacion) Replay ahora pinta el tablero real (criaturas visibles) y actualiza vida en vivo, en vez de solo texto:**
- `playMoveReplay`: clona `turn.fieldCards` (`cloneJson`, deep-copy via `toString()`) -> `snapshot`. Ordena `resolvedMoves` por `Move.resolvedOrder`. Para cada move, en su PRIMERA aparicion como `targetFieldCardId`, hace rollback de `snapshot[targetId].currentHealth/status` a `targetHealthBefore`/`targetStatusBefore` (reconstruye estado pre-turno). Pinta tablero inicial con `renderReplayBoard(snapshot, null)` (sin texto de accion).
- `playMoveStep`: cada 2000ms (recursivo via `gameRefreshHandler.postDelayed`, `isAdded()` guard) aplica `targetHealthAfter`/`targetStatusAfter` del move actual al `snapshot` y llama `renderReplayBoard(snapshot, describeMove(move, snapshot))` -> tablero se repinta entero (vida actualizada + texto de la accion actual sustituye al anterior, pues `renderReplayBoard` limpia el contenedor). Tras el ultimo move, espera 1s mas y llama `onComplete`.
- `renderReplayBoard(fieldCardsSnapshot, actionText)`: limpia `gameBoardContainer`, titulo "Resolviendo turno...", `addBoardText(actionText)` si no null, `buildBoardLists(snapshot)` + `addBoardSection` Rival/Tuyas (mismas vistas que tablero normal, sin handlers de click porque `pendingHandIndex`/`stagedActions` estan vacios en este punto).
- `buildBoardLists(fieldCards)`: extraido del bloque que antes vivia inline en `renderBoard` (separa fieldCards en `boardMyCards`/`boardOpponentCards` por `ownerId`, ordena por `position`); `renderBoard` ahora solo llama `buildBoardLists(currentTurn.fieldCards)`.
- `describeMove`/`describeFieldCard`: sin cambios, resuelven nombre de carta accion + nombre/dueño origen-destino.

**Backend `Move.java`**: nuevos campos `targetHealthBefore`, `targetHealthAfter` (Integer), `targetStatusBefore`, `targetStatusAfter` (String, nombre de `FieldCardStatus`). `GameService.resolveTurn`: captura before-values del target ANTES de aplicar el efecto (y los copia como after-values por defecto), luego sobreescribe after-values si el efecto se aplico (incluye caso de muerte -> `DEAD`/0). Si `actionCard`/`effect`/`target` invalidos o target ya `DEAD`, before==after (sin cambio, simplifica reconstruccion en Android).

**(actualizacion) Resaltado de la accion actual durante el replay:**
- `playMoveStep`: por cada move, marca en el `snapshot` la criatura origen con `_replayHighlight="attacker"` y la criatura objetivo con `_replayHighlight="target"` (helper `clearReplayHighlights` borra las marcas del paso anterior antes de poner las nuevas).
- `createFieldCardView`: nueva rama prioritaria que lee `fieldCard.optString("_replayHighlight","")` -> `"attacker"` pinta fondo azul `0xCC6699FF` (mismo tono que "origen seleccionado"), `"target"` pinta fondo rojo `0xCCFF5555` (mismo tono que "objetivo enemigo"); ambos con texto blanco. Estas claves `_replay*` son metadata local de UI, nunca se envian al backend.
- `addReplayActionText` (sustituye `addBoardText` para el texto de accion en `renderReplayBoard`): texto serif-bold 16sp sobre fondo dorado solido `0xFFE9D8A6`/texto navy `0xFF172033`, full-width con padding -- mas prominente que el texto informativo normal.

### 2026-06-11 (cont.) - Criatura muerta no actua si su turno aun no ha ocurrido

**Backend `GameService.resolveTurn`**: nuevo chequeo justo despues de capturar before/after del target -- si `source` (criatura origen del move) es `null` o `status == DEAD` (murio por un move anterior con `resolvedOrder` menor), `continue` sin aplicar efecto. El move sigue registrandose en `resolvedMoves` con su `resolvedOrder` (before==after en el target, sin cambios). Como los moves se procesan en orden de `resolvedOrder`, una criatura solo se salta su accion si murio en un move ANTERIOR -- si su accion ya se proceso, una muerte posterior no la retroactiva.

**Android `GameFragment`**: en `playMoveStep`, si `snapshot[sourceId].status=="DEAD"` (la criatura origen ya broker desde un paso anterior del replay) -> no aplica `targetHealthAfter`/`targetStatusAfter` ni resalta atacante/objetivo (`clearReplayHighlights` sin nuevas marcas). `describeMove` detecta el mismo caso y muestra `"N. {Criatura} ha caído y no puede usar {Carta}"` en vez de `"N. Carta: Origen → Objetivo"`.

### 2026-06-12 - Limpieza de procesos cloudflared huerfanos

Se detecto que `CloudflareTunnelPublisher` solo mataba el proceso `cloudflared` via shutdown hook (`stopTunnel`). Si el backend se mata duro (Stop en IDE, debug stop, `taskkill /F`, crash), el hook no corre y `cloudflared.exe` queda huerfano, ocupando el tunel/puerto entre arranques.

**Cambios en `server/src/main/java/com/tfg/tcgserver/service/tunnel/CloudflareTunnelPublisher.java`:**
- `publishTunnelUrl()` ahora llama a `killStaleCloudflaredProcesses()` antes de lanzar el tunel nuevo.
- `killStaleCloudflaredProcesses()`: extrae el nombre de proceso de `cloudflare.tunnel.command` (via `extractProcessName`, anade `.exe` en Windows si falta) y ejecuta `taskkill /F /IM <nombre>` (Windows) o `pkill -f <nombre>` (Unix). Errores se ignoran (`LOGGER.debug`), es normal que no haya proceso previo.
- `isWindows()`: detecta SO via `os.name`.
- `stopTunnel()` (shutdown hook normal) ahora ademas mata `tunnelProcess.descendants()` con `ProcessHandle::destroy`, por si `cloudflared` lanza subprocesos.

Nuevo import: `java.nio.file.Paths`.

No elimina el riesgo de proceso colgado durante la sesion si se mata la consola (imposible desde la JVM), pero garantiza limpieza en el siguiente arranque -> no se acumulan `cloudflared.exe` huerfanos.

**Nota para revisiones futuras de "procesos colgados":** se revisaron todos los `ProcessBuilder`/`ExecutorService`/`@Scheduled`/`Timer` del backend (`grep` sobre `server/src/main/java`). Unico proceso externo es `cloudflared` en `CloudflareTunnelPublisher`. Resto de hilos (matchmaking, polling Android) viven en memoria sin procesos OS asociados.

### 2026-06-12 (cont.) - Sustituido polling de 3s por long polling (partida y matchmaking)

**Motivacion:** Android refrescaba `GET /api/games/{gameId}` y `POST /api/matchmaking/find` cada 3s con un `Handler.postDelayed`. Se cambia a long polling: el cliente lanza la peticion y el backend la mantiene abierta hasta que hay cambio real o expira un timeout (~25s), devolviendo entonces; el cliente relanza inmediatamente. Tunnel de Cloudflare NO se toca (sigue como en [[2026-06-01]]/[[2026-06-12]]).

**Backend - nuevo: `GameRules.LONG_POLL_TIMEOUT_MS = 25_000`** (`server/src/main/java/com/tfg/tcgserver/models/rules/GameRules.java`).

**`Game.java`**: nuevo campo `version` (long, getter/setter), serializado junto al resto del estado.

**`ActiveGameStore.java`** (reescrito): cada partida activa tiene `Entry{game, version, waiters}`. `save()` incrementa `version` y lo escribe en `game.setVersion(...)`, luego completa los `waiters` pendientes. `remove()` marca `version=-1` y completa waiters (para que un long poll en curso sobre una partida que acaba de terminar no se quede colgado). Nuevo `awaitVersionChange(gameId, sinceVersion, timeoutMs)`: si la version actual ya difiere de `sinceVersion` devuelve de inmediato; si no, registra un `CompletableFuture` que se completa al cambiar version o por `completeOnTimeout`.

**`GameService`**: `getGame(gameId)` ahora delega en `getGame(gameId, since)`. Si la partida no esta activa (historico/no encontrada), responde directo desde `gameRepository`. Si esta activa y `since == null || since != currentVersion`, responde inmediato. Si `since == currentVersion`, espera con `awaitVersionChange` y al resolver vuelve a llamar `getGame(gameId, null)` (cubre tanto "cambio de version" como "partida recien finalizada").

**`GameController`**: `GET /api/games/{gameId}` acepta `@RequestParam(required=false) Long since`.

**`MatchmakingQueue`**: nuevo `matchWaiters` (`Map<playerId, CompletableFuture<String>>`). `storeMatchedGame` completa el waiter si existe, si no lo deja en `matchedGamesByPlayerId` (igual que antes). Nuevo `awaitMatch(playerId, timeoutMs)`: si ya hay partida asignada la devuelve al instante; si no, registra waiter con `completeOnTimeout(null, ...)`. `findOpponentFor`: si el oponente sacado de la cola es el propio jugador (entrada residual de un long poll anterior que expiro), se sustituye por la entrada nueva en vez de reencolar ambas -- evita que la cola crezca sin limite con reintentos.

**`MatchmakingService.findMatch`**: cuando no hay rival disponible, en vez de devolver `waiting` al instante, hace `matchmakingQueue.awaitMatch(playerId, GameRules.LONG_POLL_TIMEOUT_MS)` y traduce el resultado (`matched` si aparecio gameId, `waiting` si expiro el timeout).

**`application.properties`**: anadido `spring.mvc.async.request-timeout=30000` (> `LONG_POLL_TIMEOUT_MS`, evita que Tomcat corte la espera antes que el propio timeout del backend).

**Android `BackendClient`**:
- `executorService` paso de `newSingleThreadExecutor()` a `newFixedThreadPool(4)` -- con un solo hilo, una peticion de long poll (hasta ~30s) bloqueaba el resto de llamadas (submitTurnSelection, selectCreatures, etc.).
- Nuevas constantes `DEFAULT_READ_TIMEOUT_MS=5000`, `LONG_POLL_READ_TIMEOUT_MS=35000`.
- `get`/`post` ahora tienen overload con `readTimeoutMs` explicito; las versiones sin ese parametro usan `DEFAULT_READ_TIMEOUT_MS`.
- `getGame(idToken, gameId, sinceVersion, callback)`: si `sinceVersion != null`, anade `?since=` a la URL y usa `LONG_POLL_READ_TIMEOUT_MS`; si es `null`, comportamiento de siempre (timeout corto, respuesta inmediata).
- `findMatch(...)` usa `LONG_POLL_READ_TIMEOUT_MS` (el backend puede tardar hasta ~25s en responder).

**Android `GameFragment`**:
- Nuevo campo `Long lastVersion`. `renderGame` lo actualiza desde `game.optLong("version", 0)` ANTES del chequeo de "respuesta sin cambios" (asi se mantiene al dia incluso en respuestas de timeout sin cambios).
- `loadGame(showLoading)` (carga inicial / re-render tras accion local / tras mutaciones) sigue pidiendo `since=null` -> respuesta inmediata, igual que antes.
- Nuevo `pollGame()`: pide `since=lastVersion` (long poll). Es el nuevo cuerpo del `Runnable gameRefresh` (antes `() -> loadGame(false)`).
- `scheduleGameRefresh()`: de `postDelayed(gameRefresh, 3000)` a `post(gameRefresh)` (inmediato) -- el long poll del backend ya hace de espera. En error de red, `pollGame` aplica backoff manual de 3s antes de reintentar.
- Los `postDelayed(..., 2000)` del replay de turno ([[2026-06-11]]) NO se tocan, siguen siendo animacion con ritmo fijo.

**Android `DeckSelectionFragment`**: `scheduleMatchmakingPoll()` de `postDelayed(matchmakingPoll, 3000)` a `post(matchmakingPoll)` (inmediato, el backend ya espera ~25s). En error de red, backoff manual de 3s antes de reintentar.

**No verificado:** no se pudo compilar (`mvn`/`mvnw` no disponibles en este entorno) ni hacer build de Android. Revisar al compilar por primera vez.

### 2026-06-18 - Cliente web Angular creado (`angular/`)

Se creo un tercer cliente, web, en `angular/` (proyecto `tcg-web`), para interactuar con el mismo Firebase (`online-tcg-arasesp`) que Android y el backend.

Decisiones de stack (preguntadas al usuario):
- CSS plano (sin SCSS).
- NgModules clasico, no standalone components (`ng new --standalone=false`).
- AngularFire (`@angular/fire@18` + `firebase`) para Auth y Realtime Database desde el cliente.

Generado con:
```text
npx ng new tcg-web --directory=. --routing --style=css --standalone=false --skip-git --package-manager=npm
```
(ejecutado dentro de `angular/`, carpeta ya existente y vacia, por eso `--directory=.`).

Config Firebase web (de Firebase console, app registrada `online-tcg-arasesp`) puesta en `angular/src/environments/environment.ts` y `environment.development.ts` (`apiKey`, `authDomain`, `databaseURL`, `projectId`, `storageBucket`, `messagingSenderId`, `appId`). No incluye `measurementId`/analytics (no se uso `getAnalytics`).

Estructura creada:
```text
angular/src/app/app.module.ts            - registra Auth+Database de AngularFire, declara componentes
angular/src/app/app-routing.module.ts    - rutas: '' -> redirect 'login', 'login', 'register', 'home'
angular/src/app/services/auth.service.ts - login/register/logout + currentUser$ (Observable<User|null> via user(auth))
angular/src/app/services/cards.service.ts- getCards() lee nodo 'cards' de Realtime DB con listVal(keyField:'id')
angular/src/app/pages/login/             - formulario email/password, ngModel, navega a /home
angular/src/app/pages/register/          - igual que login pero createUserWithEmailAndPassword
angular/src/app/pages/home/              - muestra email usuario actual + lista cartas desde CardsService
```

**Bug encontrado y corregido:** `provideFirebaseApp`/`provideAuth`/`provideDatabase` (de `@angular/fire/*`) devuelven `EnvironmentProviders`, NO son `NgModule`/`ModuleWithProviders` -- no pueden ir en el array `imports` de `@NgModule`, deben ir en `providers`. Ponerlos en `imports` rompe la compilacion de **todo** el modulo silenciosamente en cascada: el compilador AOT reporta errores no relacionados (`NG8002: Can't bind to 'ngModel'`, `NG8004: No pipe found with name 'async'/'json'`) en todos los componentes declarados en ese modulo, en vez de senalar el error real (`TS2322: Type 'EnvironmentProviders' is not assignable to type 'any[] | Type<any> | ModuleWithProviders<{}>'`) que solo aparecio tras borrar cache (`rm -rf .angular dist`) y repetir `ng build`. Leccion: si un `ng build` con NgModules da errores de pipes/directivas "basicas" (`async`, `ngModel`, `ngIf`) que deberian funcionar, sospechar que el `@NgModule` decorator entero fallo a compilar por otro motivo y revisar canal de error completo, no solo los ultimos mensajes ni fiarse de errores en cache (limpiar `.angular/` si el mensaje no cambia entre intentos).

Tambien se anadio `CommonModule` explicito a `imports` de `AppModule` (no hizo falta tras el fix real, pero se deja porque no estorba).

Build verificado: `npx ng build` (con warning de bundle 542KB > budget 512KB, no bloqueante) y `npx ng serve` sirviendo `/login` con 200.

**Pendiente:** registrar reglas de seguridad de Firebase Realtime Database para acceso desde web (actualmente las reglas existentes son las mismas que usa Android/backend); decidir si el cliente web necesita endpoints propios del backend Java o solo lee/escribe Firebase directo para Auth+lectura de `cards`.

### 2026-06-18 (cont.) - Perfil editable, historial de partidas, cartas/mazos solo-lectura en Angular

Se amplio el cliente Angular para cubrir el resto del alcance pedido por el usuario: ver cartas/mazos (solo lectura), ver+editar perfil (username + avatar predefinido), ver historial de partidas con detalle turno a turno. Esto requirio dos endpoints nuevos en el backend Java que no existian.

**Backend nuevo:**
- `UserProfile.java`: nuevo campo `avatarId` (String) + getter/setter. Perfiles creados antes de este cambio devuelven `avatarId=null`; el frontend lo trata como "usar avatar por defecto".
- `PlayerService.createProfile`: asigna `avatarId="avatar_1"` por defecto a perfiles nuevos.
- `PlayerService.updateProfile(uid, username, avatarId)` (nuevo): `findById` -> falla si no existe perfil -> `setUsername`/`setAvatarId`/`setUpdatedAt` -> `save()` (reescritura del objeto completo, igual que el resto de mutaciones de perfil en este servicio).
- `api/dto/UpdateUserProfileRequest.java` (nuevo record): `username`, `avatarId`, ambos `@NotBlank`.
- `PlayerController`: nuevo `PUT /api/users/{uid}/profile` (requiere `requireSameUser`). Path `/profile` separado del `GET /api/users/{uid}` para no confundir "perfil completo" con "subconjunto editable".
- `GameRepository.findAll()` (nuevo): mismo patron que `CardRepository.findAll()` (`GenericTypeIndicator<Map<String,Game>>` sobre el path `"games"`).
- `api/dto/GameSummaryResponse.java` (nuevo record): `gameId, status, createdAt, startedAt, finishedAt, player1Id, player2Id, winnerId, opponentId` (este ultimo computado).
- `GameService.getGamesForPlayer(uid)` (nuevo): `gameRepository.findAll()` -> filtra por `player1Id==uid || player2Id==uid` -> mapea a `GameSummaryResponse` -> ordena por `createdAt` descendente.
- `PlayerController`: nuevo `GET /api/users/{uid}/games` (requiere `requireSameUser`); para esto `PlayerController` ahora tambien inyecta `GameService` en el constructor (antes solo tenia `PlayerService`).
- **Hallazgo importante verificado en codigo**: `GameRepository.save()` (escritura en el nodo `games/` de Firebase) **solo se llama cuando una partida termina** (`GameService.finishGame` y el camino de fin de turno dentro de `submitTurnSelection` cuando `resolveTurn` devuelve `finished=true`). Las partidas en curso viven solo en `ActiveGameStore` (memoria), nunca en Firebase. Por tanto `GameRepository.findAll()` sobre `games/` devuelve **automaticamente solo partidas FINISHED** -- no hizo falta filtrar por estado ni decidir si incluir partidas en curso.
- No se pudo compilar (`mvn`/`mvnw` no disponibles en este entorno, igual que en cambios anteriores) -- revisar al compilar por primera vez en el IDE.

**Frontend nuevo (`angular/`):**
- `HttpClientModule` + `HTTP_INTERCEPTORS` anadidos a `AppModule`. Nuevo `app/interceptors/auth.interceptor.ts`: si `auth.currentUser` existe, adjunta `Authorization: Bearer <idToken>` (via `user.getIdToken()`) a cada peticion HTTP; si no hay usuario, pasa la peticion sin tocar.
- `app/services/backend-config.service.ts` (nuevo): `BehaviorSubject<string>` seedeado con `environment.apiBaseUrl` (`http://localhost:8080` en dev y prod por ahora, pendiente URL real del tunnel Cloudflare), con lectura unica (`take(1)`) de `config/backend/baseUrl` en Realtime DB que sobreescribe el valor si existe -- mismo patron que `BackendConfig` de Android.
- `app/services/backend-api.service.ts` (nuevo): wrapper unico de `HttpClient` hacia el backend Java -- `getProfile`, `updateProfile`, `getCollection`, `getDecks`, `getDeck`, `getGames`, `getFinishedGame`. El header de auth lo pone el interceptor, este servicio no toca tokens.
- `app/models/` (nuevo): `card.model.ts`, `deck.model.ts`, `user-profile.model.ts`, `game.model.ts` -- interfaces TS que mapean 1:1 los DTOs/modelos Java (incluye `GameStatus`/`MoveType`/`FieldCardStatus`/`CardType` como uniones de string literal, ya que los enums Java serializan como su `name()`). `cards.service.ts`'s `Card` ahora extiende `CardData` de `card.model.ts` en vez de ser un tipo suelto `{id?, [key:string]:unknown}`.
- Regla seguida (y por seguir en cambios futuros): `cards` se sigue leyendo directo de Firebase (`CardsService`, dato publico sin dueno). Perfil/coleccion/mazos/partidas pasan siempre por el backend Java porque ahi vive el `requireSameUser` -- leerlos directo de RTDB duplicaria esa autorizacion en reglas de Firebase y en Java.
- `app/shared/avatars.ts` (nuevo): constante `AVATARS` con 6 avatares (`avatar_1`..`avatar_6`), `DEFAULT_AVATAR_ID`, helper `avatarSrc()`. Imagenes en `angular/public/avatars/avatar_1.svg`..`avatar_6.svg` (SVGs planos geometricos simples, creados sin assets externos -- circulo de color + figura, sin dependencias de licencia). Se sirven desde `public/` porque es la carpeta de assets ya cableada en `angular.json` para este proyecto (la version 18 con builder `application` no usa `src/assets/`, que no existe aqui).
- `app/shared/replay.ts` (nuevo, funciones puras sin DI): `buildTurnReplay(turn, cardsCatalog, myUid)` -- puerto a TypeScript del algoritmo de replay de Android (`GameFragment.playMoveReplay`/`describeMove`/`describeFieldCard`, ver entrada [[2026-06-11]]), SIN los `setTimeout` de 2s de animacion (aqui se calcula todo de una vez, sin animar): clona `turn.fieldCards`, hace rollback de cada target tocado a `targetHealthBefore/targetStatusBefore` la primera vez (mismo guard que Android), recorre `resolvedMoves` ordenados por `resolvedOrder` aplicando `targetHealthAfter/targetStatusAfter` salvo que el origen ya este `DEAD`, devuelve un `ReplayStep[]` estructurado (no string pre-formateado) para que la plantilla decida el render.
- Paginas nuevas (todas `NgModule` clasico, declaradas en `AppModule`, igual que `LoginComponent`/`HomeComponent`):
  - `pages/cards/` -- lista solo-lectura, reusa `CardsService.getCards()` sin cambios.
  - `pages/decks/` -- lista de mazos del usuario via `BackendApiService.getDecks(uid)`.
  - `pages/deck-detail/` -- `deckId` de la ruta, cruza `getDeck()` con el catalogo de `CardsService` para mostrar nombres.
  - `pages/profile/` -- `getProfile()` al cargar, formulario `ngModel` (username) + grid de avatares (click selecciona, resalta), boton guardar llama `updateProfile()`.
  - `pages/game-history/` -- `getGames(uid)`, lista con fecha/rival(`opponentId`)/resultado (`winnerId===uid`), enlaza a detalle.
  - `pages/game-detail/` -- `getFinishedGame(gameId)` + catalogo de cartas, cada `Turn` en un `<details><summary>Turno N</summary>` (nativo HTML, sin stepper con estado) con la lista de `resolvedMoves` via `buildTurnReplay`.
- Rutas anadidas en `app-routing.module.ts`: `cards`, `decks`, `decks/:deckId`, `profile`, `games`, `games/:gameId`. `home.component.html` ahora tiene `<nav>` con enlaces a las 4 secciones nuevas (antes no tenia navegacion).
- Sin guards de ruta (login/register/home tampoco los tenian); pendiente si se quiere mas adelante.

**Verificado:** `npx ng build` limpio (solo warning de presupuesto de bundle, 590KB > 512KB, no bloqueante). `npx ng serve` + smoke test con `curl` a `/cards`, `/decks`, `/profile`, `/games` -- los 4 devuelven 200.

**Pendiente:** el backend no se pudo compilar en este entorno (sin `mvn`/`mvnw`) -- compilar y probar `PUT /api/users/{uid}/profile` y `GET /api/users/{uid}/games` manualmente (curl o `dev.html`) antes de dar esto por cerrado. Tambien sigue pendiente la URL real del tunnel Cloudflare en `environment.ts` (`apiBaseUrl` de produccion hoy apunta a `localhost:8080`, solo sirve en local).

### 2026-06-18 (cont.) - /frontend-design en Angular: identidad "Chronicle Realms" extendida a la web

Se aplico `/frontend-design` al cliente Angular, que hasta ahora solo tenia CSS por defecto sin estilo. En vez de inventar una estetica nueva, se extendio la identidad visual **ya establecida y muy iterada en Android** ("Chronicle Realms", estetica "scriptorium" de manuscrito iluminado -- ver entradas `/frontend-design` del 2026-06-07 [[chronicle-realms-scriptorium-aesthetic]] y `android/app/src/main/res/values/colors.xml`), para que ambos clientes (web y movil) se vean como el mismo juego.

**Paleta** (copiada 1:1 de `colors.xml` de Android): `navy_deepest #0D1520`, `navy_dark #101828`, `navy #172033`, `gold #C9A84C`, `gold-soft/parchment #E9D8A6`, `ink #FFFFFF`, `ink_soft #E7EDF7`, `ink_muted rgba(255,255,255,.6)`. Colores de estado del tablero Android (`accent_blue #6699FF` = atacante/accion propia, `accent_red #FF5555` = objetivo/rival/peligro, `dead_gray #888888`) reutilizados en la web para el replay turno a turno (origen del move en azul, objetivo en rojo -- mismo significado que el highlight de Android, no "mio vs rival").

**Tipografia** (Google Fonts, nuevo `<link>` en `angular/src/index.html`): `Cinzel` (display, mayusculas "grabadas", para wordmark/headings) + `Oswald` condensed (UI: nav, botones, labels, uppercase tracking -- equivalente web de `sans-serif-condensed` de Android) + `Source Sans 3` (body/texto largo). Texto "nota de manuscrito" (ayudas/textos secundarios) usa Oswald italica atenuada, igual que Android usa sans-serif italica para "Sin cartas"/"Esperando turno...".

**Firma/motivo recurrente:** medallon "sello de lacre" (`.seal`, circulo con anillo dorado) y `.seal-badge` (pildora con borde dorado) -- reusado para el grid de avatares de perfil, la insignia de mazo jugable/incompleto, y el badge victoria/derrota del historial. Conecta con el vocabulario ya usado en Android ("el sello se rompe y aparecen...").

**Archivos:**
- `angular/src/styles.css` (reescrito): tokens CSS (`:root` con toda la paleta/fuentes), clases globales reutilizables -- `.panel`, `.btn-gold`/`.btn-ghost`, `.field`, `.seal`/`.seal-badge` (variantes `gold`/`muted`/`victory`/`defeat`), `.ledger`/`.ledger-row`/`.ledger-main` (listas tipo registro, usada en mazos e historial), `.auth-page`/`.auth-form`, `.eyebrow`/`.muted-note`, `.hr-gold`. Incluye `prefers-reduced-motion`.
- `angular/src/index.html`: `<link>` Google Fonts (Cinzel + Oswald + Source Sans 3).
- `app.component.ts/.html/.css` (reescrito): ahora es el "shell" persistente -- topbar con wordmark "⟡ Chronicle Realms" + nav (`Cartas/Mazos/Perfil/Historial/Salir`, oculta si no hay sesion via `*ngIf="currentUser$ | async"`). `logout()` se movio aqui desde `HomeComponent` (logica de shell, no de pagina).
- `pages/home/`: simplificado -- se quito el nav duplicado y el listado JSON crudo de cartas que eran restos de la prueba inicial (ver entrada original del 2026-06-18 al crear el proyecto). Ahora es un dashboard con 4 tiles (`Cartas/Mazos/Perfil/Historial`, simbolos heraldicos no-emoji: `✦ ❖ ⟡ §`) que enlazan a cada seccion.
- `pages/login/`, `pages/register/`: panel centrado (`.auth-page` + `.panel.auth-form`), titulos en tono "Entra al reino"/"Forja tu nombre".
- `pages/cards/`: grid de "cartas TCG" reales (`.tcg-card`, marco navy+oro para CREATURE, borde azul para ACTION, coste en circulo, stats ATK/HP/SPD o DAÑO segun tipo).
- `pages/decks/`, `pages/deck-detail/`: listas `.ledger` con `.seal-badge` de estado (jugable/incompleto).
- `pages/profile/`: grid de avatares como medallones `.seal` (circulo+anillo dorado), boton guardar dorado, confirmacion "El sello queda fijado."
- `pages/game-history/`: `.ledger` con `.seal-badge` victoria(dorado)/derrota(rojo)/en curso.
- `pages/game-detail/`: cada turno en `<details class="panel">` ("folio" desplegable), movimientos con origen en azul / objetivo en rojo, muertos en gris.

**Verificado:** `npx ng build` limpio (mismo warning de bundle preexistente, no nuevo). `npx ng serve` + `curl` a `/`, `/login`, `/register`, `/home`, `/cards`, `/decks`, `/profile`, `/games` -- todos 200.

**No verificado:** sin herramienta de captura de pantalla en este entorno, no se pudo ver el render real en navegador -- solo se verifico que compila y las rutas cargan. Revisar visualmente en un navegador real antes de considerar el diseño cerrado (contraste de colores, alineacion de los `.seal`/`.tcg-card`, tamaño de fuente Cinzel en mayusculas largas).

### 2026-06-18 (cont.) - Bug: perfil (y cualquier endpoint protegido) no cargaba en Angular -- faltaba CORS

**Sintoma:** la pagina de perfil en Angular se quedaba en blanco (sin datos, sin error visible).

**Causa raiz:** el backend nunca tuvo configuracion CORS. Toda peticion cross-origin (`http://localhost:4200` Angular -> `http://localhost:8080` Java) con header `Authorization` dispara un preflight `OPTIONS`. `FirebaseAuthenticationFilter.shouldNotFilter` no excluia `OPTIONS`, asi que el preflight (sin header `Authorization`, es preflight) recibia `401` sin cabeceras CORS -> el navegador bloqueaba la peticion real silenciosamente. Afectaba a TODOS los endpoints protegidos (`/api/users/**`, `/api/games/**` salvo los 2 publicos), no solo perfil -- `decks`/`game-history`/`game-detail` tenian el mismo problema pero no se habia notado porque ademas esos `subscribe()` tampoco tenian handler de error (fallo mudo, igual que perfil).

**Arreglo backend:**
- `FirebaseAuthenticationFilter.shouldNotFilter`: anadido `"OPTIONS".equalsIgnoreCase(request.getMethod())` a la condicion de bypass.
- `config/WebCorsConfig.java` (nuevo, `WebMvcConfigurer`): `addCorsMappings` sobre `/api/**`, origenes desde `app.cors.allowed-origins` (nueva property en `application.properties`, default `http://localhost:4200`), metodos `GET,POST,PUT,DELETE,OPTIONS`, headers `*`.
- Pendiente cuando se despliegue de verdad: anadir el dominio real (Cloudflare Tunnel o donde se hostee la web) a `app.cors.allowed-origins` (admite varios separados por comma via `@Value` en array).

**Arreglo frontend:** `profile.component.ts` -- el `subscribe()` de `getProfile` no tenia callback de error, asi que un fallo (CORS u otro) dejaba la pagina en blanco sin pista. Ahora tiene `error:` callback que rellena `this.error`, y la plantilla (`profile.component.html`) muestra ese error incluso cuando `profile` es `null` (antes el `<p *ngIf="error">` estaba anidado dentro de `*ngIf="profile"`, invisible si la carga fallaba).

**Pendiente (senalado al usuario, no implementado aun):** `decks`, `deck-detail`, `game-history`, `game-detail` tienen el mismo patron de `subscribe()` sin manejo de error -- funcionan ya que CORS esta arreglado, pero seguiran fallando mudos si hay otro problema futuro (token caducado, backend caido, etc).

**No verificado:** backend no se pudo recompilar en este entorno (sin `mvn`/`mvnw`, igual que siempre) -- el usuario debe reiniciar el backend desde su IDE para que el fix de CORS surta efecto (cambios Java no aplican en caliente).

### 2026-06-18 (cont.) - Android: avatar visible + perfil editable (paridad con la web)

Se llevo a Android lo mismo que se construyo en Angular: el `avatarId` elegido en la web ahora se ve en Android, y el perfil (username + avatar) se puede editar tambien desde Android, no solo desde la web.

**Avatares (`android/app/src/main/res/drawable/`):**
- `avatar_1.xml` .. `avatar_6.xml` (nuevos): vector drawables 64x64, mismo color/forma EXACTOS que los SVG de `angular/public/avatars/avatar_1..6.svg` (circulo de fondo + figura: triangulo/circulo/cuadrado/diamante/estrella/hoja), para que el emblema se vea igual en ambos clientes. El fondo circular se convirtio a `pathData` de dos arcos (`M2,32 A30,30 0 1,1 62,32 A30,30 0 1,1 2,32 Z`) porque los vector drawables de Android no tienen elemento `<circle>`, solo `<path>`.
- `avatar_ring_selected.xml`/`avatar_ring_unselected.xml` + `avatar_picker_ring.xml` (selector): anillo dorado cuando el boton de avatar esta seleccionado (`android:state_selected`), anillo tenue blanco si no. Se activa via `button.setSelected(true/false)` en codigo, patron estandar de Android (no hacia falta logica custom).

**`activity_profile.xml`** (usado tanto por `ProfileFragment` -- la pantalla viva -- como por el extinto `ProfileActivity`):
- El `TextView avatarText` (letra generada del username) se sustituyo por `ImageView avatarImage` dentro del mismo `FrameLayout` circular (`@drawable/avatar_background`).
- Nuevo panel "EDITAR PERFIL" (mismo estilo `info_panel_background` + eyebrow dorado que el panel "MAZO ACTIVO"): `EditText usernameInput`, fila de 6 `ImageButton avatarOption1..6` (uno por avatar, con `avatar_picker_ring` de fondo), `Button saveProfileButton` (estilo gold solido como `refreshButton`), `TextView profileStatusText` para feedback.

**`ProfileFragment.java`** (reescrito):
- `showProfile(snapshot)` ahora tambien lee `avatarId` (`snapshot.child("avatarId")`); si es `null` o no esta en la lista de 6 ids conocidos, usa `DEFAULT_AVATAR_ID = "avatar_1"` (mismo fallback que el backend/Angular).
- `selectAvatar(avatarId)`: actualiza `selectedAvatarId`, pinta `avatarImage` (`resolveAvatarDrawable` via `getResources().getIdentifier(avatarId, "drawable", ...)`, fallback `R.drawable.avatar_1` si no existe) y marca el boton correspondiente como `setSelected(true)` (resto `false`). Se llama tanto al recibir datos de Firebase como al pulsar un avatar del picker (preview inmediato antes de guardar).
- `saveProfile()`: replica el patron exacto de `DeckBuilderFragment` para escritura via backend -- `user.getIdToken(false).addOnSuccessListener(tokenResult -> backendClient.updateProfile(tokenResult.getToken(), uid, username, selectedAvatarId, callback))`, callback actualiza `profileStatusText` via `runOnUiThreadSafe` (helper nuevo, mismo nombre/patron que en `DeckBuilderFragment`). Valida que `username` no este vacio antes de llamar.
- La edicion pasa por el backend Java (`PUT /api/users/{uid}/profile`, mismo endpoint que ya usa Angular), no escribe Firebase directo -- mismo principio de autorizacion centralizada ya aplicado en la web.

**`BackendClient.java`:** nuevo metodo `updateProfile(idToken, uid, username, avatarId, callback)`, mismo patron que `updateDeck` (`PUT /api/users/{uid}/profile`, body `{"username":..., "avatarId":...}`).

**Limpieza forzada:** `ProfileActivity.java` (ya confirmado dead code -- nada lo lanza, `HomeActivity` solo usa `ProfileFragment`) referenciaba `R.id.avatarText`, que dejo de existir al cambiar el layout compartido `activity_profile.xml`. Habria roto la compilacion. Se borro el archivo entero y su `<activity android:name=".ProfileActivity">` en `AndroidManifest.xml` (en vez de mantener un campo/vista muerta solo para que compilase un archivo que nadie usa).

**Verificado:** `./gradlew :app:compileDebugJavaWithJavac -q` limpio (solo warnings de `source/target 8 obsolete`, nada nuevo). `./gradlew :app:processDebugResources -q` limpio (valida XML de layouts/drawables/manifest, sin errores) -- a diferencia de intentos anteriores, esta vez Gradle SI pudo ejecutar en este entorno (sin el error de red `Unable to establish loopback connection` de versiones previas de la entrada [[2026-05-28]]).

**No verificado:** no se instalo/ejecuto la app en emulador o dispositivo real -- solo compilacion + procesamiento de recursos. Falta probar visualmente que el picker de avatares y el guardado funcionen end-to-end contra el backend real.

### 2026-06-19 - Memoria del TFG (docx): apartado web Angular + ampliacion exhaustiva Android/Angular/Backend

Se amplio `docs/Proyecto_Chronicle_Realms.docx` (memoria oficial del TFG, fuera de `server/`) en dos pasadas, usando 3 subagentes Explore en paralelo para inventariar el codigo real de `android/`, `angular/` y `server/` antes de escribir, evitando documentar nada que no exista en el codigo.

**Pasada 1 (apartado web):** anadido bloque "Angular" en tecnologias (4.2), 4o componente "Cliente Web (Angular)" en la arquitectura cliente-servidor (6.2), y nueva subseccion "Estructura de la aplicacion Web (Angular)" (pantallas, `BackendApiService` + interceptor Bearer, los 2 endpoints nuevos `PUT /api/users/{uid}/profile` y `GET /api/users/{uid}/games`, diseno visual heredado de Android).

**Pasada 2 (ampliacion exhaustiva):**
- Nueva subseccion "API REST completa" (6.2): los ~25 endpoints de los 10 controllers, agrupados, con metodo/path/auth.
- Modelo de datos ampliado: atributos que faltaban en `UserProfile` (avatarId, mmr, eloId, cardCollection), `Card` (image, `EffectTarget`), `Deck` (cardCount, timestamps); 2 entidades nuevas (`PurchaseTransaction`, `Elo`); tabla de constantes `GameRules`/`DeckRules`.
- **Bug de documentacion corregido:** la memoria decia `activeDeckId`, pero el atributo real en `UserProfile.java` es `selectedDeckId` -- corregido en el docx.
- Codificacion (6.3) ampliada con 2 mecanismos de backend no documentados antes (long polling via `ActiveGameStore.awaitVersionChange` reutilizado por `MatchmakingQueue`; gestion del mazo de acciones -- robo/descarte/reshuffle en `drawCards`/`discardTurnCards`) y nueva subseccion "Cliente Web (Angular)" (interceptor `AuthInterceptor`, replay turno a turno en `shared/replay.ts`, equivalente web del replay animado de `GameFragment`).
- Nota anadida sobre `ShopActivity`/`CollectionActivity`/`DeckBuilderActivity`/`DeckSelectionActivity`: existen como Activities sueltas casi duplicadas de sus Fragments homonimos (los Fragments alojados en `HomeActivity` son la via de navegacion real).

**Metodo:** todas las ediciones via `python-docx` (scripts en `docs/generar_presentacion.py`, `docs/agregar_angular_docx.py`, `docs/ampliar_docx_extenso.py`), insertando parrafos con el mismo estilo/numeracion de lista (`numId=19`) que ya usaba el documento, para mantener coherencia visual. El archivo estuvo abierto en Word dos veces durante la sesion (bloqueo de escritura `PermissionError`) -- se le pidio al usuario cerrarlo antes de cada guardado.

**No verificado:** no se reviso visualmente el `.docx` resultante en Word (solo se inspecciono texto/estilos via `python-docx`); revisar maquetacion, saltos de pagina y el indice de contenidos (que en Word es un campo y puede necesitar actualizarse manualmente con F9 al abrir el archivo).
