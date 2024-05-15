package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.game.common.PokerConstants;
import poker.connection.server.game.Game;
import poker.connection.utils.VirtualThread;

import java.util.*;
public abstract class Queuer extends VirtualThread {

    private final Server server;
    protected final List<Connection> queue = new ArrayList<>();
    protected final Queue<Connection> playersRequeueing = new LinkedList<>();

    private final Map<String, Game> gameRooms = new HashMap<>();

    private static final int TIME_TO_RELAX = 10;

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
        queue.replaceAll(c -> c.getUsername().equals(connection.getUsername()) ? connection : c);
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
            if (connection.getChannel().requestMatchReconnect()){
                Game game = gameRooms.get(connection.getUsername());
                game.reconnectPlayer(connection);
            }
        } catch (ChannelException ignored) {}
    }
}
