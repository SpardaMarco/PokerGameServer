package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.game.common.PokerConstants;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RankedQueuer extends Queuer {

    private final Map<String, Threshold> playersThresholds = new HashMap<>();
    private final Map<String, ScheduledExecutorService> thresholdSchedulers = new HashMap<>();

    private static final int TIME_TO_RELAX = 10;

    public RankedQueuer(Server server) {
        super(server);
    }

    public void addToMainQueue(Connection connection) {
        try {
            if (connection.getChannel().requestMatchmaking()) {
                if (queue.stream().noneMatch(c -> c.getUsername().equals(connection.getUsername()))) {
                    queue.add(connection);
                    addPlayerThreshold(connection);
                    notify();
                    schedulePlayerThresholdUpdate(connection);
                } else {
                    updateMainQueue(connection);
                }
            }
        } catch (ChannelException ignored) {}
    }

    public synchronized void addPlayerThreshold(Connection connection) {
        Threshold threshold = new Threshold(connection.getRank());
        playersThresholds.put(connection.getUsername(), threshold);
    }

    public synchronized void removePlayerThreshold(Connection connection) {
        playersThresholds.remove(connection.getUsername());
    }

    public synchronized void updatePlayerThreshold(Connection connection) {
        Threshold threshold = playersThresholds.get(connection.getUsername());
        threshold.expand();
        notify();
    }

    public void schedulePlayerThresholdUpdate(Connection connection) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() ->
                        updatePlayerThreshold(connection),
                TIME_TO_RELAX,
                TIME_TO_RELAX,
                java.util.concurrent.TimeUnit.SECONDS
        );
        thresholdSchedulers.put(connection.getUsername(), scheduler);
    }

    public void cancelPlayerThresholdUpdate(Connection connection) {
        ScheduledExecutorService scheduler = thresholdSchedulers.get(connection.getUsername());
        if (scheduler != null) {
            scheduler.shutdown();
            thresholdSchedulers.remove(connection.getUsername());
        }
    }

    public ArrayList<Connection> tryMatchmaking() {
        ArrayList<Connection> room = new ArrayList<>();

        for (Connection player : queue) {
            Threshold threshold = playersThresholds.get(player.getUsername());
            room.add(player);
            for (Connection opponent : queue) {
                if (player.getUsername().equals(opponent.getUsername())) continue;
                if (threshold.overlaps(playersThresholds.get(opponent.getUsername()))) {
                    room.add(opponent);
                    if (room.size() == PokerConstants.NUM_PLAYERS) {
                        break;
                    }
                }
            }
            if (room.size() != PokerConstants.NUM_PLAYERS) room.clear();
            else break;
        }

        return room;
    }

    public void createGame() {
        ArrayList<Connection> connections = tryMatchmaking();
        if (!connections.isEmpty()) {
            boolean allAlive = true;
            for (Connection connection : connections) {
                if (connection.isBroken()) {
                    allAlive = false;
                    queue.remove(connection);
                    removePlayerThreshold(connection);
                    cancelPlayerThresholdUpdate(connection);
                }
            }
            if (allAlive) {
                for (Connection connection : connections) {
                    queue.remove(connection);
                    removePlayerThreshold(connection);
                    cancelPlayerThresholdUpdate(connection);
                }
                startGame(connections);
            }
        }
    }
}
