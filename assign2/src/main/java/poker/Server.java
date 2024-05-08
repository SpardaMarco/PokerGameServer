package poker;

import poker.connection.protocol.Channel;
import poker.connection.protocol.Connection;
import poker.connection.server.authentication.AuthenticationManager;
import poker.connection.server.database.DatabaseInterface;
import poker.connection.server.queue.QueueManager;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Server {
    private final Queue<String> playersQueue = new LinkedList<>();
    private final Map<String, Channel> connections = new Hashtable<>();
    private final AuthenticationManager authenticationManager;
    private final QueueManager queueManager;
    private final boolean loggingEnabled;
    private final boolean rankedMode;
    private final DatabaseInterface database = new DatabaseInterface();

    public static void main(String[] args) throws SQLException {
        if (args.length < 1) {
            System.out.println("Usage: java TimeServer <port> [-l] [-r]");
            return;
        }

        int port = Integer.parseInt(args[0]);
        boolean loggingEnabled = false;
        boolean rankedMode = false;
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "-l":
                    loggingEnabled = true;
                    break;
                case "-r":
                    rankedMode = true;
                    break;
                default:
                    System.out.println("Usage: java TimeServer <port> [-l] [-r]");
                    return;
            }
        }

        Server server = new Server(port, loggingEnabled, rankedMode);
        server.init();
    }

    private Server(int port, boolean loggingEnabled, boolean rankedMode) {
        this.loggingEnabled = loggingEnabled;
        this.rankedMode = rankedMode;
        this.authenticationManager = new AuthenticationManager(this, port);
        this.queueManager = new QueueManager(this);
    }

    public Queue<String> getPlayersQueue() {
        return playersQueue;
    }

    public Map<String, Channel> getConnections() {
        return connections;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public DatabaseInterface getDatabase() {
        return database;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public boolean isRankedMode() {
        return rankedMode;
    }

    private void init() {
        authenticationManager.start();
        queueManager.start();
        System.out.println("Press [ENTER] to stop the server\n");
        new Scanner(System.in).nextLine();
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
