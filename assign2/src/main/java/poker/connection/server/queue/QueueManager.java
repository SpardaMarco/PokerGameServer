package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Channel;
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
                    List<Connection> connections = new ArrayList<>();

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
                    new Game(server, connections).start();
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
