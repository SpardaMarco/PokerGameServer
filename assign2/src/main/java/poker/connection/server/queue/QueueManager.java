package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.game.common.PokerConstants;
import poker.connection.server.game.Game;
import poker.connection.utils.VirtualThread;

import java.util.*;

public class QueueManager extends VirtualThread {
    private final Server server;
    private final Queue<Connection> mainQueue = new LinkedList<>();
    private final Queue<Connection> playersRequeueing = new LinkedList<>();
    private final Map<String, Game> rooms = new HashMap<>();
    private final Map<String, Threshold> playerThresholds = new HashMap<>();

    public QueueManager(Server server) {
        this.server = server;
    }

    public boolean isRankedMode() {
        return server.isRankedMode();
    }

    public synchronized void addPlayerToMainQueue(Connection connection) {
        if (rooms.get(connection.getUsername()) != null) {
            reconnectPlayerToGame(connection);
        } else if (mainQueue.stream().noneMatch(c -> c.getUsername().equals(connection.getUsername()))) {
            mainQueue.add(connection);

            if (isRankedMode()) {
                addPlayerThreshold(connection);
            }

            notify();
        } else {
            updateMainQueue(connection);
        }
    }

    public synchronized void updateMainQueue(Connection connection) {
        Queue<Connection> tempQueue = new LinkedList<>();

        while (!mainQueue.isEmpty()) {
            Connection c = mainQueue.poll();
            if (!c.getUsername().equals(connection.getUsername())) {
                tempQueue.add(c);
            } else {
                tempQueue.add(connection);
            }
        }
        mainQueue.addAll(tempQueue);
    }

    public synchronized void requeuePlayers(List<Connection> connections) {
        this.playersRequeueing.addAll(connections);
        notify();
    }

    public synchronized void removePlayerFromRequeue(Connection connection) {
        this.playersRequeueing.remove(connection);
    }

    public synchronized void assignPlayerToRoom(Connection connection, Game game) {
        this.rooms.put(connection.getUsername(), game);
    }

    public synchronized void removePlayerFromRoom(Connection connection) {
        this.rooms.remove(connection.getUsername());
    }

    public synchronized void addPlayerThreshold(Connection connection) {
        Threshold threshold = new Threshold(connection.getRank() - 1000, connection.getRank() + 1000);
        playerThresholds.put(connection.getUsername(), threshold);
    }

    public synchronized void removePlayerThreshold(Connection connection) {
        playerThresholds.remove(connection.getUsername());
    }

    public synchronized void updatePlayerThreshold(Connection connection) {
        Threshold threshold = playerThresholds.get(connection.getUsername());
        threshold.setLowerBound(threshold.getLowerBound() - 1000);
        threshold.setUpperBound(threshold.getUpperBound() + 1000);
    }

    public void startGame(ArrayList<Connection> connections) {
        Game game = new Game(server, connections);

        for (Connection connection : connections) {
            assignPlayerToRoom(connection, game);
        }

        game.start();
    }

    public void reconnectPlayerToGame(Connection connection) {
        Game game = rooms.get(connection.getUsername());
        game.reconnectPlayer(connection);
    }

    @Override
    protected void run() {
        while (true) {
            synchronized (this) {
                if (isRankedMode()) {

                    // TBD: Implement the logic for ranked mode

                } else {

                    if (mainQueue.size() < PokerConstants.NUM_PLAYERS) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        ArrayList<Connection> connections = new ArrayList<>();
                        boolean allAlive = true;

                        for (int i = 0; i < PokerConstants.NUM_PLAYERS; i++) {
                            Connection connection = mainQueue.poll();
                            assert connection != null;

                            if (connection.isBroken()) {
                                allAlive = false;
                                for (int j = 0; j < i; j++) {
                                    addPlayerToMainQueue(connections.get(j));
                                }
                                break;
                            }

                            connections.add(connection);
                        }

                        if (allAlive) startGame(connections);
                    }
                }

                if (!this.playersRequeueing.isEmpty()) {
                    for (Connection connection : this.playersRequeueing) {
                        new Requeuer(this, connection).start();
                    }
                }
            }
        }
    }
}
