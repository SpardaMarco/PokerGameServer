package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.channels.ServerChannel;
import poker.game.common.PokerConstants;
import poker.connection.server.game.Game;
import poker.connection.utils.VirtualThread;

import java.sql.SQLException;
import java.util.*;

public class QueueManager extends VirtualThread {
    private final Server server;
    private final Queue<Connection> playersRequeuing = new LinkedList<>();
    private final Map<String, Game> rooms = new HashMap<>();

    public QueueManager(Server server) {
        this.server = server;
    }

    public synchronized void requeuePlayers(List<Connection> connections) {
        this.playersRequeuing.addAll(connections);
        notify();
    }

    public void addPlayerToMainQueue(Connection connection) {
        server.queuePlayer(connection);
    }

    public synchronized void addPlayerToRoom(Connection connection, Game game) {
        this.rooms.put(connection.getUsername(), game);
    }

    public synchronized void removePlayerFromRequeue(Connection connection) {
        this.playersRequeuing.remove(connection);
    }

    public synchronized void removePlayerFromRoom(Connection connection) {
        this.rooms.remove(connection.getUsername());
    }

    public void startGame(ArrayList<Connection> connections) {
        Game game = new Game(server, connections);

        for (Connection connection : connections) {
            addPlayerToRoom(connection, game);
        }

        game.start();
    }

    @Override
    protected void run() {
        while (true) {
            synchronized (this) {
                if (server.getPlayersQueue().size() < PokerConstants.NUM_PLAYERS) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    ArrayList<Connection> connections = new ArrayList<>();

                    for (int i = 0; i < PokerConstants.NUM_PLAYERS; i++) {
                        String player = server.getPlayersQueue().poll();
                        ServerChannel channel = server.getConnections().get(player);

                        try {
                            if (server.getDatabase().userExists(player)) {
                                if (server.getDatabase().getUserSession(player) != null) {
                                    String token = server.getDatabase().getUserSession(player);
                                    connections.add(new Connection(player, token, channel));
                                } else {
                                    throw new RuntimeException("User session not found");
                                }
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    startGame(connections);
                }
            }

            if (!this.playersRequeuing.isEmpty()) {
                for (Connection connection : this.playersRequeuing) {
                    new Requeuer(this, connection).start();
                }
            }
        }
    }
}
