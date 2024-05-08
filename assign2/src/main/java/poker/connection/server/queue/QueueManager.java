package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Channel;
import poker.game.common.PokerConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueueManager extends Thread {
    private final Server server;

    public QueueManager(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
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
                            }
                            else {
                                throw new RuntimeException("User session not found");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                // TBD: Send information to the game thread
            }
        }
    }
}
