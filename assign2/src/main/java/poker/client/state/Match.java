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

            switch (message.getState()) {
                case MATCH_DISPLAY -> {
                    return handleMatchDisplay(message);
                }
                case MATCH_MOVE -> {
                    return handleMatchPlay(message);
                }
                case REQUEUE -> {
                    return handleRequeue();
                }
                default -> {
                    throw new UnexpectedMessageException("Unexpected message received: " + message);
                }
            }
        } catch (ClosedConnectionException e) {
            System.out.println("Connection to the server was lost.");
            return null;
        } catch (ChannelException e) {
            System.out.println("Error communicating with the server:\n" + e.getMessage());
            return null;
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
        Pair<String, Integer> action = gui.askMove(gameState);

        try {
            channel.sendPlayerMove(action.getFirst(), action.getSecond().toString());
        } catch (Exception e) {
            System.out.println("Error communicating with the server:\n" + e.getMessage());
            return null;
        }

        return this;
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
                System.out.println("Connection to the server was lost.");
                return null;
            } catch (ChannelException e) {
                System.out.println("Error communicating with the server:\n" + e.getMessage());
                return null;
            }
        } while (true);
    }
}
