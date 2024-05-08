package poker.client.state;

import poker.Client;
import poker.connection.protocol.channels.ClientChannel;
import poker.connection.protocol.message.Message;

public class Matchmaking implements ClientState {

    @Override
    public ClientState handle(ClientChannel channel) {

        System.out.println("Waiting for other players to join...");

        Message message = channel.getGameStartRequest();

        return null;
    }
}
