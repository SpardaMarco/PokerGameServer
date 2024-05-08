package poker.client.state;

import poker.Client;

public interface ClientState {

    ClientState handle(Client client);
}
