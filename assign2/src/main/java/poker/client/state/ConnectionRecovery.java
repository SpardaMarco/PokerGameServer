package poker.client.state;

import poker.Client;
import poker.connection.protocol.message.Message;

import java.util.Scanner;

public class ConnectionRecovery implements ClientState {

    @Override
    public ClientState handle(Client client) {
        String sessionToken = client.getSessionToken();

        if (sessionToken != null) {
            System.out.println("Do you wish to recover your previous session? (Y/N)");
            String input = new Scanner(System.in).nextLine();

            if (input.equalsIgnoreCase("Y")) {
                Message response = client.getChannel().recoverSession(sessionToken);
                if (response == null) {
                    return null;
                }
                System.out.println(response.getBody());
                if (response.isOk()) {
                    client.saveSessionToken(response.getAttribute("sessionToken"));
                    return new Matchmaking();
                }
            }
        }

        return new Authentication();
    }


}
