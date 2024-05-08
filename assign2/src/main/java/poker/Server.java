package poker;

import poker.connection.protocol.Channel;
import poker.connection.protocol.Connection;
import poker.connection.server.authentication.AuthenticationManager;
import poker.connection.server.database.DatabaseInterface;
import poker.connection.server.queue.QueueManager;

import java.sql.SQLException;
import java.util.*;

public class Server {
    private final Queue<String> playersQueue = new LinkedList<>();
    private final Map<String, Channel> connections = new Hashtable<>();
    private final AuthenticationManager authenticationManager;
    private final QueueManager queueManager;
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
        this.queueManager = new QueueManager(this);
        this.loggingEnabled = loggingEnabled;
    }

    public Queue<String> getPlayersQueue() { return playersQueue; }

    public Map<String, Channel> getConnections() { return connections; }

    public QueueManager getQueueManager() { return queueManager; }

    public DatabaseInterface getDatabase() {
        return database;
    }

    private void init() {
        authenticationManager.start();
        queueManager.start();
    }

    public synchronized void queuePlayer(Connection connection) {
        playersQueue.add(connection.getUsername());
        connections.put(connection.getUsername(), connection.getChannel());
        synchronized (queueManager) {
            queueManager.notify();
        }

        if (this.loggingEnabled) {
            System.out.println("Player " + connection.getUsername() + " has joined the game");
            System.out.println("Players in queue: " + playersQueue.size());
        }
    }
}
