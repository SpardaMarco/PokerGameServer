package poker.clientState;

import poker.Client;
import poker.connection.protocol.message.Message;

import java.util.Scanner;

public class Authentication implements ClientState {

    @Override
    public ClientState handle(Client client) {
        System.out.println("Enter your username: ");
        String username = new Scanner(System.in).nextLine();
        System.out.println("Enter your password: ");
        String password = new Scanner(System.in).nextLine();

        Message response = client.getChannel().authenticate(username, password);
        if (response == null) {
            return null;
        }
        System.out.println(response.getBody());

        if (response.isOk()) {
            client.saveSessionToken(response.getAttribute("sessionToken"));
            return new Matchmaking();
        }

        return new Authentication();
    }
}
