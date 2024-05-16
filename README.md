# CPD Projects

CPD Projects of group T12G15.

Group members:

1. João Ramos (up202108743@edu.fe.up.pt)
2. Marco Costa (up202108821@edu.fe.up.pt)
3. Tiago Viana (up201807126@edu.fe.up.pt)


## Notes
This project was developed using Java SE 21.
The list of dependencies used are the following:
- [JDBC](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/)
- [jBCrypt](https://www.mindrot.org/projects/jBCrypt/)
- [JSON](https://github.com/stleary/JSON-java)
- [GSON](https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/Gson.html)
## Run Server

```bash
$ java Server <port> [-l] [-r]
```

- \<port\> must be a valid port, ex: 8000
- [-l] is optional and enables logging
- [-r] is optional and enables ranked mode

## Run Client

```bash
$ java Client <host> <port>
```

- \<host\> must be a valid host, ex: localhost
- \<port\> must be a valid port, ex: 8000

## Database

The database is stored in the `database` folder inside the project `src` folder. We are using a SQLite database.

In order to reset and populate the database with the initial data, run the following command:

```bash
$ TODO DB
```

The database contains only one table, `users`, capable of storing the following fields:
- username (PK)
- password
- rank
- session_token
- session_expiration

For the purpose of this project, the database is pre-populated with the following users:

| username | password (on login) | rank   |
|----------|----------|--------|
| marco    | marco    | 1000   |
| tiago    | tiago    | 2000   |
| ramos    | ramos    | 1000   |
| joao     | joao     | 1500   |
| rita     | rita     | 500    |
| jorge    | jorge    | -10000 |
| afonso   | afonso   | 5000   |
| camilla  | camilla  | 5000   |
| baquero  | baquero  | 0      |
| alberto  | alberto  | 0      |
| veronica | veronica | 0      |

The passwords are stored using BCrypt hash with *salt*.

A new token is generated using BCrypt hash with *salt* and the username when the user authenticates, and it is stored in the database. This token is valid for 24 hours.

The token can be used to reconnect to the server without the need to authenticate again.

## Communication

The communication between the server and the client is done using a custom message protocol and channels.
Most of the classes implemented for this purpose are within the `poker.connection.protocol` package.

The communication can be divided into two main parts:
- **Messages**
- **Channels**

### Messages

Messages are the objects that are sent between the server and the client. They contain 4 important fields:
- **State**: represents the stage of the application in which the message is being sent (e.g. CONNECTION_END, AUTHENTICATION, MATCH_RECONNECT, etc.)
- **Status**: represents the status of the message (REQUEST, OK, ERROR)
- **Body**: a string that contains direct feedback about the message, usually provided by the server (e.g. *"User successfully logged in"*, etc.).
- **Data**: contains differnt attributes that are specific to the message being sent (e.g. username and password, etc.)

This messages are being sent using the JSON format. The GSON library is used for the serialization and deserialization of more complex objects.

### Channels

The channels are objects of the abstract class `Channel`. 
This class is extended by two other classes:
- **ClientChannel**: represents the channel that is used by the client to communicate with the server.
- **ServerChannel**: represents the channel that is used by the server to communicate with the client.

The channel stores the Socket and the associated Input and Output streams, and creates a new layer of abstraction for the communication between both parties.

### Channel Exceptions

During the communication, many different types of errors can occur and for a multitude of reasons. In order to classify these errors many types of exceptions were defined within the `poker.connection.protocol.exceptions` package.
These exceptions are thrown when an error occurs and are caught by the server or the client, depending on the context. They include:
- **ClosedConnectionException**: thrown when the channel is closed.
- **RequestTimeoutException**: thrown when a request times out.
- **TokenMismatchException**: thrown when one of the parties does not recognize the received token.
- **UnexpectedMessageException**: thrown when an unexpected message is received, depending on both the state and the status of the message.

## Game

The game implemented is a simple version of "Poker Texas Hold'em". The game is played between 6 players, the server being the dealer.

The most of the game logic and model is implemented in the `game` directory.

The number of players is easily configurable by changing the value of the constant `NUM_PLAYERS` in the `poker.game.common.PokerConstants.java` class.

For easier manipulation of the game by the server, the main class `Poker` represents the current game instance state.

## Architecture
