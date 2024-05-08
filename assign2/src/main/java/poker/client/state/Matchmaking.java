package poker.client.state;

import poker.connection.protocol.channels.ClientChannel;

public class Matchmaking implements ClientState {

    @Override
    public ClientState handle(ClientChannel channel) {

        System.out.println("Waiting for other players to join...");

        channel.handleGameStartRequest();

        System.out.println("Game is starting...");

        return new Match();
    }
}
