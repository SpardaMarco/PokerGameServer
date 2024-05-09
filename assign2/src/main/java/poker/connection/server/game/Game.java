package poker.connection.server.game;

import poker.Server;
import poker.connection.protocol.Channel;
import poker.connection.protocol.Connection;
import poker.connection.protocol.channels.ServerChannel;
import poker.connection.protocol.message.Message;
import poker.connection.utils.VirtualThread;
import poker.game.common.GameState;
import poker.game.common.PokerPlayer;
import poker.game.server.Poker;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends VirtualThread {
    private final Server server;
    private final ArrayList<Connection> playerConnections;
    private final Poker poker;
    private final ReentrantLock playerConnectionsLock;

    public Game(Server server, ArrayList<Connection> playerConnections) {
        this.server = server;
        this.playerConnections = playerConnections;
        this.playerConnectionsLock = new ReentrantLock();
        ArrayList<String> playerUsernames = new ArrayList<>();
        for (Connection connection : playerConnections) {
            playerUsernames.add(connection.getUsername());
        }
        poker = new Poker(playerUsernames);
    }

    public boolean isPlayerInGame(String username) {
        boolean result = false;
        playerConnectionsLock.lock();
        for (Connection connection : playerConnections) {
            if (connection.getUsername().equals(username)) {
                result = true;
                break;
            }
        }
        playerConnectionsLock.unlock();
        return result;
    }

    public boolean reconnectPlayer(Connection newConnection) {
        playerConnectionsLock.lock();
        int index = -1;

        for (int i = 0; i < playerConnections.size(); i++) {
            if (playerConnections.get(i).getUsername().equals(newConnection.getUsername())) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            playerConnectionsLock.unlock();
            return false;
        }
        playerConnections.set(index, newConnection);
        playerConnectionsLock.unlock();
        return true;
    }

    private void sendGameState() {
        playerConnectionsLock.lock();
        for (int i = 0; i < playerConnections.size(); i++) {
            sendGameState(i);
        }
        playerConnectionsLock.unlock();
    }

    private void sendGameState(int player) {
        playerConnectionsLock.lock();
        ServerChannel channel = playerConnections.get(player).getChannel();
        playerConnectionsLock.unlock();
        GameState gameState = poker.getGameStateToSend(player);

        channel.sendGameState(null, gameState);
    }

    private void notifyPlayers() {
        for (Connection connection: playerConnections) {
            connection.getChannel().notifyGameStart();
        }
    }

    @Override
    protected void run() {
        // Assumes that all players have joined (i.e. we sent a message to confirm connection before starting the game)
        if (server.isLoggingEnabled()) {
            System.out.println("Starting game with " + playerConnections.size() + " players");
        }

        notifyPlayers();
        play();
        if (server.isLoggingEnabled()) {
            System.out.println("Game finished");
        }
        if (server.isRankedMode()) {
            updateRanks();
        }
        finishGame();
    }

    private void play() {
        while (!poker.getIsGameOver()) {
            while (!poker.getIsHandOver()) {
                int currentPlayer = poker.getCurrPlayer();
                sendGameState();
                makePlay(currentPlayer);
            }
            sendGameState();
            poker.endHand();
        }
        sendGameState();
    }

    private void makePlay(int player) {

        playerConnectionsLock.lock();
        ServerChannel channel = playerConnections.get(player).getChannel();
        playerConnectionsLock.unlock();
        if (channel == null || channel.isOpen()) {
            if (server.isLoggingEnabled()) {
                System.out.println("Player " + player + " disconnected");
            }
            poker.takeAction(PokerPlayer.PLAYER_ACTION.FOLD, 0);
            return;
        }

        Message message = channel.getPlayerMove("It's your turn");
        if (message == null) {
            if (server.isLoggingEnabled()) {
                System.out.println("Player " + player + " disconnected");
            }
            poker.takeAction(PokerPlayer.PLAYER_ACTION.FOLD, 0);
            return;
        }

        String action = message.hasAttribute("action") ? message.getAttribute("action") : "";
        int amount = message.hasAttribute("amount") ? message.getIntAttribute("amount") : 0;
        switch (action) {
            case "fold":
                poker.takeAction(PokerPlayer.PLAYER_ACTION.FOLD, amount);
                break;
            case "call":
                poker.takeAction(PokerPlayer.PLAYER_ACTION.CALL, amount);
                break;
            case "bet":
                poker.takeAction(PokerPlayer.PLAYER_ACTION.BET, amount);
                break;
            case "check":
                poker.takeAction(PokerPlayer.PLAYER_ACTION.CHECK, amount);
                break;
            case "all_in":
                poker.takeAction(PokerPlayer.PLAYER_ACTION.ALL_IN, amount);
                break;
            default:
                throw new RuntimeException("Invalid action: " + action);
        }
    }

    private void finishGame() {
        playerConnectionsLock.lock();
        for (Connection connection : playerConnections) {
            server.getQueueManager().removePlayerFromRoom(connection);
        }
        server.getQueueManager().requeuePlayers(playerConnections);
        playerConnectionsLock.unlock();

    }

    private void updateRanks() {
        if (server.isLoggingEnabled()) {
            System.out.println("Updating rankings");
        }
        playerConnectionsLock.lock();
        for (PokerPlayer player : poker.getGameWinners()) {
            Connection connection = null;
            for (Connection c : playerConnections) {
                if (c.getUsername().equals(player.getUsername())) {
                    connection = c;
                    break;
                }
            }
            if (connection != null) {
                server.getDatabase().updateRank(connection.getUsername(), player.getMoney() / 100);
                connection.setRank(server.getDatabase().getUserRank(connection.getUsername()));
            }
        }
        playerConnectionsLock.unlock();
    }
}
