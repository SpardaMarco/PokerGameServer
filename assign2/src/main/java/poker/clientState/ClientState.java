package poker.clientState;

import poker.Client;
import poker.connection.protocol.channels.ClientChannel;

public interface ClientState {

    ClientState handle(Client client);
}
