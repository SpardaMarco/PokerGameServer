package poker;

import poker.connection.protocol.Connection;
import poker.connection.server.authentication.AuthenticationManager;
import poker.connection.server.database.DatabaseInterface;
import poker.connection.server.queue.QueueManager;

import java.sql.SQLException;
import java.util.Scanner;

public class Server {
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
                    System.out.println("Logging enabled");
                    break;
                case "-r":
                    rankedMode = true;
                    System.out.println("Ranked mode enabled");
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
        disconnect();
    }

    public synchronized void queuePlayer(Connection connection) {
        queueManager.addPlayerToMainQueue(connection);
    }

    private synchronized void disconnect() {
        // send disconnect message to all clients
    }
}
