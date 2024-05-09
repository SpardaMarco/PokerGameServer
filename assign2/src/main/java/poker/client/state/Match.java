package poker.client.state;

import com.google.gson.Gson;
import poker.connection.protocol.Connection;
import poker.connection.protocol.channels.ClientChannel;
import poker.connection.protocol.message.Message;
import poker.game.client.PokerClientGUI;
import poker.game.common.GameState;
import poker.utils.Pair;

import java.util.Scanner;

import static poker.connection.protocol.message.State.*;

public class Match implements ClientState {

    PokerClientGUI gui = new PokerClientGUI();

    @Override
    public ClientState handle(ClientChannel channel) {
        Message message;
        try {
            message = channel.getRequest();
        } catch (Exception e) {
            System.out.println("Failed communicating with the server during Match");
            return null;
        }

        if (message.getState().equals(MATCH_DISPLAY)) {
            String gameStateJson = message.getAttribute("gameState");
            GameState gameState = new Gson().fromJson(gameStateJson, GameState.class);
            gui.display(gameState);

            return this;
        } else if (message.getState().equals(MATCH_MOVE)) {
            String gameStateJson = message.getAttribute("gameState");
            GameState gameState = new Gson().fromJson(gameStateJson, GameState.class);
            Pair<String, Integer> action = gui.askMove(gameState);

            try {
                channel.sendPlayerMove(action.getFirst(), action.getSecond().toString());
            } catch (Exception e) {
                System.out.println("Failed communicating with the server during match");
                return null;
            }

            return this;
        } else if (message.getState().equals(REQUEUE)) {
            System.out.println("Do you want to requeue? (Y/N)");

            try {
                String response = new Scanner(System.in).nextLine();
                while (true) {
                    if (response.equalsIgnoreCase("Y")) {
                        channel.sendRequeueResponse(true);
                        return new Matchmaking();
                    } else if (response.equalsIgnoreCase("N")) {
                        channel.sendRequeueResponse(false);
                        return null;
                    } else {
                        System.out.println("Invalid input. Please enter Y or N.");
                        response = new Scanner(System.in).nextLine();
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed communicating with the server during requeueing:\n" + e.getMessage());
                return null;
            }
        }

        return null;
    }

}
