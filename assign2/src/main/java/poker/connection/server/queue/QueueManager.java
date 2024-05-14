package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.game.common.PokerConstants;
import poker.connection.server.game.Game;
import poker.connection.utils.VirtualThread;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class QueueManager extends VirtualThread {
    private final Server server;
    private final Queue<Connection> mainQueue = new LinkedList<>();
    private final List<Connection> rankedQueue = new ArrayList<>();
    private final Queue<Connection> playersRequeueing = new LinkedList<>();
    private final Map<String, Game> rooms = new HashMap<>();
    private final Map<String, Threshold> playerThresholds = new HashMap<>();
    private final Map<String, ScheduledExecutorService> playerSchedulers = new HashMap<>();

    private static final int TIME_TO_RELAX = 10;

    public QueueManager(Server server) {
        this.server = server;
    }

    public boolean isRankedMode() {
        return server.isRankedMode();
    }

    public synchronized void queuePlayer(Connection connection) {
        if (rooms.get(connection.getUsername()) != null) {
            if (reconnectPlayerToGame(connection)) {
                return;
            }
        }
        addToMainQueue(connection);
    }

    public void addToMainQueue(Connection connection) {
        try {
            if (!connection.getChannel().requestMatchmaking()) {
                return;
            }
        } catch (ChannelException e) {
            return;
        }
        if (isRankedMode()) {
            if (rankedQueue.stream().noneMatch(c -> c.getUsername().equals(connection.getUsername()))) {
                rankedQueue.add(connection);
                addPlayerThreshold(connection);
                notify();
                schedulePlayerThresholdUpdate(connection);
            } else {
                updateMainQueue(connection);
            }
        }
        else {
            if (mainQueue.stream().noneMatch(c -> c.getUsername().equals(connection.getUsername()))) {
                mainQueue.add(connection);
                notify();
            } else {
                updateMainQueue(connection);
            }
        }
    }

    public synchronized void updateMainQueue(Connection connection) {
        if (isRankedMode()) {
            rankedQueue.replaceAll(c -> c.getUsername().equals(connection.getUsername()) ? connection : c);
            return;
        }

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
        Threshold threshold = new Threshold(connection.getRank());
        playerThresholds.put(connection.getUsername(), threshold);
    }

    public synchronized void removePlayerThreshold(Connection connection) {
        playerThresholds.remove(connection.getUsername());
    }

    public synchronized void updatePlayerThreshold(Connection connection) {
        Threshold threshold = playerThresholds.get(connection.getUsername());
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
        playerSchedulers.put(connection.getUsername(), scheduler);
    }

    public void cancelPlayerThresholdUpdate(Connection connection) {
        ScheduledExecutorService scheduler = playerSchedulers.get(connection.getUsername());
        if (scheduler != null) {
            scheduler.shutdown();
            playerSchedulers.remove(connection.getUsername());
        }
    }

    public void startGame(ArrayList<Connection> connections) {
        Game game = new Game(server, connections);

        for (Connection connection : connections) {
            assignPlayerToRoom(connection, game);
        }

        game.start();
    }

    public boolean reconnectPlayerToGame(Connection connection) {

        try {
            if (!connection.getChannel().requestMatchReconnect())
                return false;
        } catch (ChannelException e) {
            return false;
        }
        Game game = rooms.get(connection.getUsername());
        game.reconnectPlayer(connection);

        return true;
    }

    public ArrayList<Connection> tryMatchmaking() {
        ArrayList<Connection> room = new ArrayList<>();

        for (Connection player : rankedQueue) {
            Threshold threshold = playerThresholds.get(player.getUsername());
            room.add(player);
            for (Connection opponent : rankedQueue) {
                if (player.getUsername().equals(opponent.getUsername())) continue;
                if (threshold.overlaps(playerThresholds.get(opponent.getUsername()))) {
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

    @Override
    protected void run() {
        while (true) {
            synchronized (this) {
                if (isRankedMode()) {
                   if (rankedQueue.size() < PokerConstants.NUM_PLAYERS) {
                       try {
                           wait();
                       } catch (InterruptedException e) {
                           throw new RuntimeException(e);
                       }
                   }
                   else {
                          ArrayList<Connection> connections = tryMatchmaking();
                          if (!connections.isEmpty()) {
                              boolean allAlive = true;
                                for (Connection connection : connections) {
                                    if (connection.isBroken()) {
                                        allAlive = false;
                                        rankedQueue.remove(connection);
                                        removePlayerThreshold(connection);
                                        cancelPlayerThresholdUpdate(connection);
                                    }
                                }
                                if (allAlive) {
                                    for (Connection connection : connections) {
                                        rankedQueue.remove(connection);
                                        removePlayerThreshold(connection);
                                        cancelPlayerThresholdUpdate(connection);
                                    }
                                    startGame(connections);
                                }
                          }
                   }
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
                                    queuePlayer(connections.get(j));
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
