# Modelo JSON para Firebase Realtime Database

Este modelo adapta el juego a Firebase Realtime Database usando un catalogo global de cartas, usuarios con mazos anidados e historico de partidas terminadas. Las partidas activas y el matchmaking viven en memoria dentro del servidor Java.

## Estructura principal

```json
{
  "users": {},
  "elos": {},
  "cards": {},
  "games": {}
}
```

## Users

El `id` del usuario debe ser el mismo `uid` de Firebase Auth.

Cada usuario tiene una lista de mazos en `decks`. Dentro de cada mazo, las cartas no duplican toda la informacion de la carta: solo guardan `cardId` y `quantity`.

Cada usuario tambien tiene `purchaseHistory`, donde se guardan las transacciones realizadas con las monedas del juego.

```json
{
  "users": {
    "Uz7yw3OeFjhCS2z3masM4RWanxc2": {
      "username": "TestPlayer",
      "email": "test@test.es",
      "coins": 0,
      "mmr": 0,
      "eloId": "bronze",
      "selectedDeckId": "deck_001",
      "createdAt": "2026-05-14T08:00:00Z",
      "updatedAt": "2026-05-14T08:00:00Z",
      "purchaseHistory": {
        "purchase_001": {
          "id": "purchase_001",
          "itemId": "pack_basic_001",
          "itemName": "Sobre basico",
          "type": "PURCHASE",
          "coinAmount": 100,
          "balanceBefore": 500,
          "balanceAfter": 400,
          "description": "Compra de un sobre basico",
          "createdAt": "2026-05-20T10:00:00Z",
          "cardRewards": {
            "card_fireball": 2,
            "card_fire_dragon": 1
          }
        }
      },
      "cardCollection": {
        "card_fireball": {
          "cardId": "card_fireball",
          "quantity": 2,
          "firstObtainedAt": "2026-05-20T10:00:00Z",
          "updatedAt": "2026-05-20T10:00:00Z"
        },
        "card_fire_dragon": {
          "cardId": "card_fire_dragon",
          "quantity": 1,
          "firstObtainedAt": "2026-05-20T10:00:00Z",
          "updatedAt": "2026-05-20T10:00:00Z"
        }
      },
      "decks": {
        "deck_001": {
          "name": "Mazo inicial",
          "playable": true,
          "cards": {
            "deck_card_001": {
              "cardId": "card_fireball",
              "quantity": 20
            },
            "deck_card_002": {
              "cardId": "card_fire_dragon",
              "quantity": 5
            }
          },
          "cardCount": {
            "action": 20,
            "creature": 5,
            "total": 25
          }
        }
      }
    }
  }
}
```

## Cards globales

El nodo `cards` contiene el catalogo global. Las criaturas tienen estadistica `speed`, usada para resolver el orden de acciones simultaneas.

```json
{
  "cards": {
    "card_fire_dragon": {
      "name": "Dragon de Fuego",
      "type": "CREATURE",
      "image": "cards/fire_dragon.png",
      "text": "Criatura ofensiva con alto ataque.",
      "stats": {
        "attack": 5,
        "health": 4,
        "cost": 3,
        "speed": 7
      }
    },
    "card_fireball": {
      "name": "Bola de Fuego",
      "type": "ACTION",
      "image": "cards/fireball.png",
      "text": "Inflige 3 puntos de dano a una criatura.",
      "effect": {
        "target": "ENEMY_CREATURE",
        "damage": 3,
        "cost": 2
      }
    }
  }
}
```

## Tienda y sobres

La tienda se expone desde el servidor. Por ahora los sobres de ejemplo se definen en codigo y el servidor asegura que las cartas de ejemplo existan en `cards` cuando se consulta o abre un sobre.

Al abrir un sobre:

- Se resta el precio del saldo del usuario.
- Se guarda una compra en `users/{uid}/purchaseHistory`.
- Se anaden las cartas obtenidas en `users/{uid}/cardCollection`.
- La operacion sobre monedas, compra y coleccion se hace con una transaccion de Firebase.

## Reglas de partida

- Antes de empezar, cada jugador selecciona 3 de sus 5 criaturas.
- 2 criaturas son publicas y activas.
- 1 criatura queda oculta boca abajo en reserva.
- Las 2 criaturas no seleccionadas quedan fuera de la partida.
- Los turnos son simultaneos.
- Cada turno tiene 90 segundos para que ambos jugadores seleccionen acciones.
- Al inicio de cada turno, cada jugador roba 5 cartas del mazo de acciones.
- Cada jugador puede jugar hasta 2 acciones por turno, una por cada criatura activa.
- Cada accion debe indicar la criatura que la usa y la criatura objetivo.
- Cuando ambos jugadores han enviado acciones, el turno se resuelve por velocidad de las criaturas.
- Gana el jugador que mata las 3 criaturas rivales.

## Games

El nodo `games` guarda el historico de partidas terminadas. Las partidas en curso viven en memoria del servidor.

```json
{
  "games": {
    "game_001": {
      "status": "FINISHED",
      "createdAt": "2026-05-14T08:10:00Z",
      "startedAt": "2026-05-14T08:11:00Z",
      "finishedAt": "2026-05-14T08:30:00Z",
      "player1Id": "Uz7yw3OeFjhCS2z3masM4RWanxc2",
      "player2Id": "otherFirebaseUid",
      "currentTurnId": "turn_001",
      "winnerId": "Uz7yw3OeFjhCS2z3masM4RWanxc2",
      "players": {
        "Uz7yw3OeFjhCS2z3masM4RWanxc2": {
          "deckId": "deck_001",
          "deadCreatures": 0
        },
        "otherFirebaseUid": {
          "deckId": "deck_002",
          "deadCreatures": 3
        }
      },
      "creatureSelections": {
        "Uz7yw3OeFjhCS2z3masM4RWanxc2": {
          "activeCreatureCardIds": ["card_fire_dragon", "card_water_guardian"],
          "hiddenCreatureCardId": "card_stone_titan",
          "submittedAt": "2026-05-14T08:10:30Z"
        },
        "otherFirebaseUid": {
          "activeCreatureCardIds": ["card_shadow_wolf", "card_ice_golem"],
          "hiddenCreatureCardId": "card_leaf_beast",
          "submittedAt": "2026-05-14T08:10:35Z"
        }
      },
      "turns": {
        "turn_001": {
          "number": 1,
          "startedAt": "2026-05-14T08:11:00Z",
          "selectionDeadlineAt": "2026-05-14T08:12:30Z",
          "endedAt": "2026-05-14T08:11:42Z",
          "fieldCards": {
            "field_001": {
              "ownerId": "Uz7yw3OeFjhCS2z3masM4RWanxc2",
              "cardId": "card_fire_dragon",
              "currentHealth": 4,
              "position": 1,
              "visibility": "PUBLIC",
              "status": "ACTIVE"
            },
            "field_003": {
              "ownerId": "Uz7yw3OeFjhCS2z3masM4RWanxc2",
              "cardId": "card_stone_titan",
              "currentHealth": 8,
              "position": 3,
              "visibility": "HIDDEN",
              "status": "BENCH"
            }
          },
          "playerSelections": {
            "Uz7yw3OeFjhCS2z3masM4RWanxc2": {
              "submittedAt": "2026-05-14T08:11:20Z",
              "actions": {
                "action_001": {
                  "playerId": "Uz7yw3OeFjhCS2z3masM4RWanxc2",
                  "type": "PLAY_ACTION",
                  "cardId": "card_fireball",
                  "sourceFieldCardId": "field_001",
                  "targetFieldCardId": "field_004",
                  "createdAt": "2026-05-14T08:11:20Z"
                }
              }
            }
          },
          "resolvedMoves": {
            "resolved_move_001": {
              "playerId": "Uz7yw3OeFjhCS2z3masM4RWanxc2",
              "type": "PLAY_ACTION",
              "cardId": "card_fireball",
              "sourceFieldCardId": "field_001",
              "targetFieldCardId": "field_004",
              "speed": 7,
              "resolvedOrder": 1,
              "createdAt": "2026-05-14T08:11:20Z"
            }
          }
        }
      }
    }
  }
}
```

## Endpoints de juego

```text
GET /api/cards
GET /api/cards/{cardId}
PUT /api/cards/{cardId}
DELETE /api/cards/{cardId}

GET /api/users/{uid}
POST /api/users
GET /api/users/{uid}/collection
GET /api/users/{uid}/purchases
POST /api/users/{uid}/purchases
GET /api/users/{uid}/decks
GET /api/users/{uid}/decks/{deckId}
POST /api/users/{uid}/decks
PUT /api/users/{uid}/decks/{deckId}
DELETE /api/users/{uid}/decks/{deckId}
POST /api/users/{uid}/decks/{deckId}/select

POST /api/matchmaking/find

GET /api/shop/packs
POST /api/shop/packs/{packId}/open

POST /api/games/{gameId}/creatures
POST /api/games/{gameId}/turn-selection
POST /api/games/{gameId}/finish
GET /api/games/{gameId}
GET /api/games/history/{gameId}
```

Salvo endpoints publicos de prueba/login, Android debe enviar el token de Firebase Auth en cada peticion:

```text
Authorization: Bearer FIREBASE_ID_TOKEN
```

## Matchmaking

No se guarda en Firebase. El servidor Java mantiene la cola en memoria y crea una partida activa cuando empareja a dos jugadores.

## Estados recomendados

```json
{
  "gameStatus": ["WAITING_PLAYERS", "SELECTING_CREATURES", "IN_PROGRESS", "FINISHED", "CANCELLED"],
  "cardType": ["CREATURE", "ACTION"],
  "moveType": ["PLAY_ACTION", "CHANGE_CREATURE", "SURRENDER"],
  "fieldCardVisibility": ["PUBLIC", "HIDDEN"],
  "fieldCardStatus": ["ACTIVE", "BENCH", "DEAD"]
}
```

## Reglas importantes

- No guardes contrasenas en Realtime Database. Firebase Auth ya las gestiona.
- Las cartas completas viven en `cards/{cardId}`.
- Los mazos viven dentro de `users/{uid}/decks`.
- Las cartas dentro de un mazo solo guardan `cardId` y `quantity`.
- Para guardar un mazo, el usuario debe tener esas cartas en `users/{uid}/cardCollection`.
- Meter una carta en un mazo no la elimina ni bloquea de la coleccion; la misma carta puede usarse en varios mazos.
- Un mazo incompleto se puede guardar, pero queda con `playable: false`.
- Solo los mazos con 20 acciones y 5 criaturas quedan con `playable: true`.
- Las compras viven dentro de `users/{uid}/purchaseHistory`.
- Cada compra guarda el coste en monedas, saldo anterior, saldo posterior y fecha.
- La coleccion de cartas del usuario vive dentro de `users/{uid}/cardCollection`.
- `games` guarda historico de partidas terminadas, no partidas activas.
- Las partidas activas viven en memoria dentro del servidor Java.
- Cada turno guarda su propia copia de `fieldCards`.
- La logica de validar jugadas y resolver velocidades debe estar en el servidor Java, no en Android.
