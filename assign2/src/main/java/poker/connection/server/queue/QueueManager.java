package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Channel;
import poker.connection.protocol.Connection;
import poker.connection.protocol.channels.ServerChannel;
import poker.game.common.PokerConstants;
import poker.connection.utils.VirtualThread;

import java.sql.SQLException;
import java.util.*;

public class QueueManager extends VirtualThread {
    private final Server server;
    private final Map<String, ServerChannel> playersRequeuing = new Hashtable<>();
    private final Queue<Connection> playersQueue = new LinkedList<>();

    public QueueManager(Server server) {
        this.server = server;
    }

    public synchronized void requeuePlayers(Map<String, ServerChannel> connections) {
        this.playersRequeuing.putAll(connections);
        notify();
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
                    List<String> players = new ArrayList<>();
                    List<Channel> channels = new ArrayList<>();
                    List<String> tokens = new ArrayList<>();

                    for (int i = 0; i < PokerConstants.NUM_PLAYERS; i++) {
                        players.add(server.getPlayersQueue().poll());
                        channels.add(server.getConnections().get(players.get(i)));

                        try {
                            if (server.getDatabase().userExists(players.get(i))) {
                                if (server.getDatabase().getUserSession(players.get(i)) != null) {
                                    tokens.add(server.getDatabase().getUserSession(players.get(i)));
                                } else {
                                    throw new RuntimeException("User session not found");
                                }
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                // TBD: Send information to the game thread
            }

            if (!this.playersRequeuing.isEmpty()) {
                // TBD: Logic to requeue players
            }
        }
    }
}
