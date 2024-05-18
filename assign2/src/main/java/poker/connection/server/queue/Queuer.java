package poker.connection.server.queue;

import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.exceptions.ClosedConnectionException;
import poker.connection.server.game.Game;
import poker.connection.utils.VirtualThread;
import poker.game.common.PokerConstants;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Queuer extends VirtualThread {
    private final Server server;
    protected final List<Connection> queue = new ArrayList<>();
    protected final Queue<Connection> playersRequeueing = new LinkedList<>();
    protected final ReentrantLock queueLock = new ReentrantLock();
    protected final ReentrantLock requeueLock = new ReentrantLock();
    protected final ReentrantLock gameRoomsLock = new ReentrantLock();
    private final Map<String, Game> gameRooms = new HashMap<>();
    private final HashSet<Requeuer> requeuers = new HashSet<>();

    public Queuer(Server server) {
        this.server = server;
    }

    @Override
    protected void run() {
        while (!this.isInterrupted()) {
            synchronized (this) {
                queueLock.lock();
                if (queue.size() < PokerConstants.NUM_PLAYERS) {
                    queueLock.unlock();
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                } else {
                    createGame();
                    queueLock.unlock();
                }
                requeueLock.lock();
                while (!this.playersRequeueing.isEmpty()) {
                    Connection connection = this.playersRequeueing.poll();
                    Requeuer requeuer = new Requeuer(this, connection);
                    requeuer.start();
                    requeuers.add(requeuer);
                }
                requeueLock.unlock();
            }
        }

        requeueLock.lock();
        for (Requeuer requeuer : requeuers) {
            requeuer.interrupt();
        }
        requeueLock.unlock();
    }

    public abstract void createGame();

    public synchronized void queuePlayer(Connection connection) {
        gameRoomsLock.lock();
        if (gameRooms.get(connection.getUsername()) != null) {
            reconnectPlayerToGame(connection);
            gameRoomsLock.unlock();
        } else {
            gameRoomsLock.unlock();
            addToMainQueue(connection);
        }
    }

    public abstract void addToMainQueue(Connection connection);

    public synchronized void updateMainQueue(Connection connection) {
        int index = -1;
        queueLock.lock();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUsername().equals(connection.getUsername())) {
                index = i;
                break;
            }
        }
        if (index != -1 && this.server.isLoggingEnabled()) {
            System.out.println("Player not found in queue when updating main queue");
        }
        Connection oldConnection = queue.get(index);
        try {
            oldConnection.getChannel().requestConnectionEnd("Another connection was found for your account");
        } catch (ClosedConnectionException e) {
            if (server.isLoggingEnabled()) {
                System.out.println("Error while disconnecting old connection for player " + connection.getUsername());
            }
        }
        queue.set(index, connection);
        queueLock.unlock();
    }

    public synchronized void requeuePlayers(List<Connection> connections) {
        requeueLock.lock();
        this.playersRequeueing.addAll(connections);
        requeueLock.unlock();
        notify();
    }

    public synchronized void removePlayerFromRequeue(Connection connection) {
        requeueLock.lock();
        this.playersRequeueing.remove(connection);
        requeueLock.unlock();
    }

    public synchronized void assignPlayerToRoom(Connection connection, Game game) {
        gameRoomsLock.lock();
        this.gameRooms.put(connection.getUsername(), game);
        gameRoomsLock.unlock();
    }

    public synchronized void removePlayerFromRoom(Connection connection) {
        gameRoomsLock.lock();
        this.gameRooms.remove(connection.getUsername());
        gameRoomsLock.unlock();
    }

    public void startGame(ArrayList<Connection> connections) {
        Game game = new Game(server, connections);

        for (Connection connection : connections) {
            assignPlayerToRoom(connection, game);
        }

        game.start();
    }

    public void reconnectPlayerToGame(Connection connection) {

        try {
            if (connection.getChannel().requestMatchReconnect()) {
                gameRoomsLock.lock();
                Game game = gameRooms.get(connection.getUsername());
                game.reconnectPlayer(connection);
                gameRoomsLock.unlock();
            }
        } catch (ChannelException ignored) {
        }
    }
}
