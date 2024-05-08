package poker.client.state;

import poker.Client;
import poker.connection.protocol.channels.ClientChannel;

public interface ClientState {

    ClientState handle(ClientChannel channel);
}
