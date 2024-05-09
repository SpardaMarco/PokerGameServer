package poker.client.state;

import com.google.gson.Gson;
import poker.connection.protocol.channels.ClientChannel;
import poker.connection.protocol.message.Message;
import poker.game.client.PokerClientGui;
import poker.game.common.GameState;

import static poker.connection.protocol.message.State.MATCH_DISPLAY;
import static poker.connection.protocol.message.State.MATCH_MOVE;

public class Match implements ClientState {

    PokerClientGui gui = new PokerClientGui();

    @Override
    public ClientState handle(ClientChannel channel) {

        Message message = channel.getRequest();

        if (message.getState().equals(MATCH_DISPLAY)) {
            String gameStateJson = message.getAttribute("gameState");
            GameState gameState = new Gson().fromJson(gameStateJson, GameState.class);
            gui.display(gameState);

            return this;
        }

        else if (message.getState().equals(MATCH_MOVE)) {
            return null;
        }


        return null;
    }
}
