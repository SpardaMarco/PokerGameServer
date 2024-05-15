package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.game.common.PokerConstants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SimpleQueuer extends Queuer {

    public SimpleQueuer(Server server) {
        super(server);
    }

    @Override
    protected void run() {
        while (true) {
            synchronized (this) {
                if (queue.size() < PokerConstants.NUM_PLAYERS) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
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

                while (!this.playersRequeueing.isEmpty()) {
                    Connection connection = this.playersRequeueing.poll();
                    new Requeuer(this, connection).start();
                }
            }
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
