package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.exceptions.ClosedConnectionException;
import poker.connection.server.game.Game;
import poker.connection.utils.VirtualThread;

import java.util.*;

public abstract class Queuer extends VirtualThread {

    private final Server server;
    protected final List<Connection> queue = new ArrayList<>();
    protected final Queue<Connection> playersRequeueing = new LinkedList<>();
    private final Map<String, Game> gameRooms = new HashMap<>();

    public Queuer(Server server) {
        this.server = server;
    }

    @Override
    protected abstract void run();

    public synchronized void queuePlayer(Connection connection) {
        if (gameRooms.get(connection.getUsername()) != null) {
            reconnectPlayerToGame(connection);
        } else {
            addToMainQueue(connection);
        }
    }

    public abstract void addToMainQueue(Connection connection);

    public synchronized void updateMainQueue(Connection connection) {
        int index = -1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUsername().equals(connection.getUsername())) {
                index = i;
                break;
            }
        }
        if (index != -1 && this.server.isLoggingEnabled()) {
            System.out.println("Player not found in queue when updating main queue");
        }
        Connection oldConnection = queue.get(index);
        try {
            oldConnection.getChannel().requestConnectionEnd("Another connection was found for your account");
        } catch (ClosedConnectionException e) {
            if (server.isLoggingEnabled()) {
                System.out.println("Error while disconnecting old connection for player " + connection.getUsername());
            }
        }
        queue.set(index, connection);
    }

    public synchronized void requeuePlayers(List<Connection> connections) {
        this.playersRequeueing.addAll(connections);
        notify();
    }

    public synchronized void removePlayerFromRequeue(Connection connection) {
        this.playersRequeueing.remove(connection);
    }

    public synchronized void assignPlayerToRoom(Connection connection, Game game) {
        this.gameRooms.put(connection.getUsername(), game);
    }

    public synchronized void removePlayerFromRoom(Connection connection) {
        this.gameRooms.remove(connection.getUsername());
    }

    public void startGame(ArrayList<Connection> connections) {
        Game game = new Game(server, connections);

        for (Connection connection : connections) {
            assignPlayerToRoom(connection, game);
        }

        game.start();
    }

    public void reconnectPlayerToGame(Connection connection) {

        try {
            if (connection.getChannel().requestMatchReconnect()) {
                Game game = gameRooms.get(connection.getUsername());
                game.reconnectPlayer(connection);
            }
        } catch (ChannelException ignored) {
        }
    }
}
