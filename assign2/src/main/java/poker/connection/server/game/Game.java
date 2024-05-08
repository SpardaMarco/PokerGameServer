package poker.connection.server.game;

import com.google.gson.Gson;
import poker.Server;
import poker.connection.protocol.channels.ServerChannel;
import poker.connection.protocol.message.Message;
import poker.game.common.GameStateToSend;
import poker.game.common.PokerPlayer;
import poker.game.server.Poker;
import poker.utils.VirtualThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends VirtualThread {
    private final Server server;
    private final ArrayList<ServerChannel> playerConnections;
    private final ArrayList<String> playerTokens;
    private final ArrayList<String> playerUsernames;
    private final Poker poker;
    private final ReentrantLock playerTokensLock;

    public Game(Server server, ArrayList<String> playerUsernames, ArrayList<String> playerTokens, ArrayList<ServerChannel> playerConnections) {
        this.server = server;
        this.playerConnections = playerConnections;
        this.playerTokens = playerTokens;
        this.playerUsernames = playerUsernames;
        this.playerTokensLock = new ReentrantLock();
        poker = new Poker(playerUsernames);
    }

    public boolean isPlayerInGame(String username) {
        playerTokensLock.lock();
        boolean result = playerUsernames.contains(username);
        playerTokensLock.unlock();
        return result;
    }

    public boolean swapToken(String oldToken, String newToken, ServerChannel newChannel) {
        playerTokensLock.lock();
        int index = playerTokens.indexOf(oldToken);
        if (index == -1) {
            playerTokensLock.unlock();
            return false;
        }
        playerTokens.set(index, newToken);
        playerConnections.set(index, newChannel);
        playerTokensLock.unlock();
        return true;
    }

    private void sendGameState() {
        playerTokensLock.lock();
        for (int i = 0; i < playerConnections.size(); i++) {
            sendGameState(i);
        }
        playerTokensLock.unlock();
    }

    private void sendGameState(int player) {
        playerTokensLock.lock();
        ServerChannel channel = playerConnections.get(player);
        playerTokensLock.unlock();
        GameStateToSend gameState = poker.getGameStateToSend(player);

        Gson gson = new Gson();
        String gameStateJson = gson.toJson(gameState);
        Map<String, Object> data = Map.of("gameState", gameStateJson);

        channel.sendGameState("Display this game state", data);
    }

    @Override
    protected void run() {
        // Assumes that all players have joined (i.e. we sent a message to confirm connection before starting the game)
        System.out.println("Starting game with " + playerConnections.size() + " players");
        play();
        System.out.println("Game finished");
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

        playerTokensLock.lock();
        ServerChannel channel = playerConnections.get(player);
        playerTokensLock.unlock();
        if (channel == null || !channel.isClosed()) {
            System.out.println("Player " + player + " disconnected");
            poker.takeAction(PokerPlayer.PLAYER_ACTION.FOLD, 0);
            return;
        }

        Message message = channel.getPlayerMove("It's your turn");
        if (message == null) {
            System.out.println("Player " + player + " disconnected");
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
        Map<String, ServerChannel> connections = new HashMap<>();
        playerTokensLock.lock();
        for (int i = 0; i < playerConnections.size(); i++) {
            connections.put(playerUsernames.get(i), playerConnections.get(i));
        }
        playerTokensLock.unlock();
//        server.getQueueManager().requeuePlayers(playerUsernames, connections);
    }
}
