package poker;

import poker.connection.protocol.Channel;
import poker.connection.server.authentication.AuthenticationManager;
import poker.connection.server.database.DatabaseInterface;

import java.sql.SQLException;
import java.util.*;

public class Server {
    private final Queue<String> playersQueue = new LinkedList<>();
    private final Dictionary<String, Channel> connections = new Hashtable<>();
    private final AuthenticationManager authenticationManager;
    private final boolean loggingEnabled;
    private final DatabaseInterface database = new DatabaseInterface();

    public static void main(String[] args) throws SQLException {
        if (args.length < 1) {
            System.out.println("Usage: java TimeServer <port> [-l]");
            return;
        }

        int port = Integer.parseInt(args[0]);

        if (args.length == 2 && args[1].equals("-l")) {
            new Server(port, true).init();
        } else if (args.length == 2) {
            System.out.println("Usage: java TimeServer <port> [-l]");
        } else {
            new Server(port, false).init();
        }
    }

    private Server(int port, boolean loggingEnabled) {
        this.authenticationManager = new AuthenticationManager(this, port);
        this.loggingEnabled = loggingEnabled;
    }

    public DatabaseInterface getDatabase() {
        return database;
    }

    private void init() {
        authenticationManager.start();
    }

    public synchronized void queuePlayer(String player, Channel socket) {
        playersQueue.add(player);
        connections.put(player, socket);

        if (this.loggingEnabled) {
            System.out.println("Player " + player + " has joined the game");
            System.out.println("Players in queue: " + playersQueue.size());
        }
    }
}
