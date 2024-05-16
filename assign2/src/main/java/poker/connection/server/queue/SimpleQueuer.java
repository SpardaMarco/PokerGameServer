package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.game.common.PokerConstants;

import java.util.ArrayList;

public class SimpleQueuer extends Queuer {

    public SimpleQueuer(Server server) {
        super(server);
    }

    public void createGame(){
        ArrayList<Connection> connections = new ArrayList<>(
                queue.subList(0, PokerConstants.NUM_PLAYERS)
        );
        boolean allAlive = true;
        for (Connection connection: connections) {
            if (connection.isBroken()) {
                allAlive = false;
                queue.remove(connection);
                break;
            }
        }

        if (allAlive) {
            for (Connection connection: connections) {
                queue.remove(connection);
            }
            startGame(connections);
        }
    }

    @Override
    public void addToMainQueue(Connection connection) {

        try {
            if (connection.getChannel().requestMatchmaking()) {
                if (queue.stream().noneMatch(c -> c.getUsername().equals(connection.getUsername()))) {
                    queue.add(connection);
                    notify();
                } else {
                    updateMainQueue(connection);
                }
            }
        } catch (ChannelException ignored) {}
    }
}
