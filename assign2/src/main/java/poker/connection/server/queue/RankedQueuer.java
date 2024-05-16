package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.game.common.PokerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

public class RankedQueuer extends Queuer {

    private final Map<String, Threshold> playersThresholds = new HashMap<>();
    private final Map<String, ScheduledExecutorService> thresholdSchedulers = new HashMap<>();
    private final ReentrantLock thresholdLock = new ReentrantLock();
    private final ReentrantLock schedulerLock = new ReentrantLock();

    private static final int TIME_TO_RELAX = 10;

    public RankedQueuer(Server server) {
        super(server);
    }

    public void addToMainQueue(Connection connection) {
        try {
            if (connection.getChannel().requestMatchmaking()) {
                queueLock.lock();
                if (queue.stream().noneMatch(c -> c.getUsername().equals(connection.getUsername()))) {
                    queue.add(connection);
                    addPlayerThreshold(connection);
                    schedulePlayerThresholdUpdate(connection);
                    queueLock.unlock();
                    notify();
                } else {
                    updateMainQueue(connection);
                    queueLock.unlock();
                }
            }
        } catch (ChannelException ignored) {
        }
    }

    public synchronized void addPlayerThreshold(Connection connection) {
        Threshold threshold = new Threshold(connection.getRank());
        thresholdLock.lock();
        playersThresholds.put(connection.getUsername(), threshold);
        thresholdLock.unlock();
    }

    public synchronized void removePlayerThreshold(Connection connection) {
        thresholdLock.lock();
        playersThresholds.remove(connection.getUsername());
        thresholdLock.unlock();
    }

    public synchronized void updatePlayerThreshold(Connection connection) {
        thresholdLock.lock();
        Threshold threshold = playersThresholds.get(connection.getUsername());
        threshold.expand();
        thresholdLock.unlock();
        notify();
    }

    public void schedulePlayerThresholdUpdate(Connection connection) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ExecutorService updater = Executors.newVirtualThreadPerTaskExecutor();
        scheduler.scheduleAtFixedRate(() ->
                        updater.execute(() -> updatePlayerThreshold(connection)),
                TIME_TO_RELAX,
                TIME_TO_RELAX,
                java.util.concurrent.TimeUnit.SECONDS
        );
        thresholdLock.lock();
        thresholdSchedulers.put(connection.getUsername(), scheduler);
        thresholdLock.unlock();
    }

    public void cancelPlayerThresholdUpdate(Connection connection) {
        thresholdLock.lock();
        ScheduledExecutorService scheduler = thresholdSchedulers.get(connection.getUsername());
        if (scheduler != null) {
            scheduler.shutdown();
            thresholdSchedulers.remove(connection.getUsername());
        }
        thresholdLock.unlock();
    }

    public ArrayList<Connection> tryMatchmaking() {
        ArrayList<Connection> room = new ArrayList<>();

        queueLock.lock();
        thresholdLock.lock();
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
        thresholdLock.unlock();
        queueLock.unlock();

        return room;
    }

    public void createGame() {
        ArrayList<Connection> connections = tryMatchmaking();
        if (!connections.isEmpty()) {
            boolean allAlive = true;
            for (Connection connection : connections) {
                if (connection.isBroken()) {
                    allAlive = false;
                    queueLock.lock();
                    queue.remove(connection);
                    queueLock.unlock();
                    removePlayerThreshold(connection);
                    cancelPlayerThresholdUpdate(connection);
                }
            }
            if (allAlive) {
                for (Connection connection : connections) {
                    queueLock.lock();
                    queue.remove(connection);
                    queueLock.unlock();
                    removePlayerThreshold(connection);
                    cancelPlayerThresholdUpdate(connection);
                }
                startGame(connections);
            }
        }
    }
}
