package poker.client.state;

import com.google.gson.Gson;
import poker.connection.protocol.channels.ClientChannel;
import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.exceptions.ClosedConnectionException;
import poker.connection.protocol.exceptions.UnexpectedMessageException;
import poker.connection.protocol.message.Message;
import poker.game.client.PokerClientGUI;
import poker.game.common.GameState;
import poker.utils.Pair;
import poker.utils.UserInput;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static poker.connection.protocol.message.State.*;

public class Match extends ClientState {

    PokerClientGUI gui = new PokerClientGUI();

    public Match(ClientChannel channel) {
        super(channel);
    }

    @Override
    public ClientState handle() {
        try {
            Message message = channel.getRequest();
            return parseMessage(message);
        } catch (ClosedConnectionException e) {
            System.out.println("Connection to the server was lost.\n" + e.getMessage());
            return null;
        } catch (ChannelException e) {
            System.out.println("Error communicating with the server:\n" + e.getMessage());
            return null;
        }
    }

    private ClientState parseMessage(Message message) throws UnexpectedMessageException {

        switch (message.getState()) {
            case MATCH_DISPLAY -> {
                return handleMatchDisplay(message);
            }
            case MATCH_PLAY -> {
                return handleMatchPlay(message);
            }
            case REQUEUE -> {
                return handleRequeue();
            }
            default -> {
                throw new UnexpectedMessageException("Unexpected message received: " + message);
            }
        }
    }

    private ClientState handleMatchDisplay(Message message) {
        String gameStateJson = message.getAttribute("gameState");
        GameState gameState = new Gson().fromJson(gameStateJson, GameState.class);
        gui.display(gameState);
        return this;
    }

    private ClientState handleMatchPlay(Message message) {

        String gameStateJson = message.getAttribute("gameState");
        GameState gameState = new Gson().fromJson(gameStateJson, GameState.class);

        Future<Pair<String, Integer>> future = Executors.newSingleThreadExecutor().submit(
                () -> gui.askMove(gameState)
        );

        final AtomicReference<Boolean> channelIsAliveWrapper = new AtomicReference<>(false);
        Thread timer = new Thread(
                () -> {
                    try {
                        channel.getServerTimeOut();
                        channelIsAliveWrapper.set(true);
                    } catch (ClosedConnectionException e) {
                        System.out.println("Connection to the server was lost.\n" + e.getMessage());
                        channelIsAliveWrapper.set(false);
                    } catch (ChannelException e) {
                        System.out.println("Error communicating with the server:\n" + e.getMessage());
                        channelIsAliveWrapper.set(false);
                    } finally {
                        future.cancel(true);
                    }
                }
        );

        Pair<String, Integer> action;

        try {
            timer.start();
            action = future.get();
            timer.interrupt();
        } catch (CancellationException e) {
            if (channelIsAliveWrapper.get() != null && !channelIsAliveWrapper.get()) {
                System.out.println("Connection to the server was lost.");
                return null;
            }
            System.out.println("Timeout reached.");
            return new Match(channel);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error getting user input:\n" + e.getMessage());
            return new Match(channel);
        }

        try {
            channel.sendPlayerMove(action.getFirst(), action.getSecond().toString());
        } catch (Exception e) {
            System.out.println("Error communicating with the server:\n" + e.getMessage());
            return null;
        }

        return new Match(channel);
    }

    private ClientState handleRequeue() {

        System.out.println("Do you want to requeue? (Y/N)");
        do {
            String response = new UserInput().nextLine();
            try {
                if (response.equalsIgnoreCase("Y")) {
                    channel.sendRequeueResponse(true);
                    Message matchmakingMessage = channel.getRequest();
                    if (matchmakingMessage.getState().equals(MATCHMAKING)) {
                        channel.acceptMatchmaking();
                        return new Matchmaking(channel);
                    } else {
                        throw new UnexpectedMessageException("Unexpected message received after requeueing: " + matchmakingMessage);
                    }
                } else if (response.equalsIgnoreCase("N")) {
                    channel.sendRequeueResponse(false);
                    return null;
                } else {
                    System.out.println("Invalid input. Please enter Y or N.");
                }
            } catch (ClosedConnectionException e) {
                System.out.println("Connection to the server was lost.\n" + e.getMessage());
                return null;
            } catch (ChannelException e) {
                System.out.println("Error communicating with the server:\n" + e.getMessage());
                return null;
            }
        } while (true);
    }
}
