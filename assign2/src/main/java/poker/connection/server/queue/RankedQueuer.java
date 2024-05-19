package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.game.common.PokerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        System.out.println("Current updated thresholds:");
        for (Connection player : queue) {
            System.out.println(
                    player.getUsername() + ": " + player.getRank() + " | " +
                    playersThresholds.get(player.getUsername()).getLowerBound() + " - " +
                    playersThresholds.get(player.getUsername()).getUpperBound()
            );
        }
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

    public ArrayList<Connection> findSuitableOpponents(List<Connection> players, ArrayList<Connection> currentRoom) {

        if (currentRoom.size() == PokerConstants.NUM_PLAYERS) {
            return currentRoom;
        }

        Connection suitablePlayer = null;
        for (Connection player : players) {
            boolean suitable = true;
            for (Connection roomPlayer : currentRoom) {
                if (!playersThresholds.get(player.getUsername()).contains(roomPlayer.getRank()) ||
                        !playersThresholds.get(roomPlayer.getUsername()).contains(player.getRank())) {
                    suitable = false;
                    break;
                }
            }
            if (suitable)  {
                suitablePlayer = player;
                break;
            }
        }
        if (suitablePlayer == null) {
            return new ArrayList<>();
        }
        currentRoom.add(suitablePlayer);
        players.remove(suitablePlayer);
        return findSuitableOpponents(players, currentRoom);
    }

    public ArrayList<Connection> tryMatchmaking() {
        ArrayList<Connection> room = findSuitableOpponents(new ArrayList<>(queue), new ArrayList<>());
        if (server.isLoggingEnabled()) {
            if (room.isEmpty()) {
                System.out.println("No suitable opponents found\n");
            } else {
                System.out.println("Room created with players:");
                for (Connection roomPlayer : room) {
                    System.out.println(roomPlayer.getUsername() + ": " +
                            playersThresholds.get(roomPlayer.getUsername()).getLowerBound() + " - " +
                            playersThresholds.get(roomPlayer.getUsername()).getUpperBound());
                }
                System.out.println();
            }
        }
        return room;
    }

    public boolean createGame() {
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
                return true;
            }
        }
        return false;
    }
}
