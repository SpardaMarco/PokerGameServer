package poker.client.state;

import poker.client.LocalToken;
import poker.connection.protocol.channels.ClientChannel;
import poker.connection.protocol.message.Message;

import java.util.Scanner;

public class Authentication implements ClientState {

    @Override
    public ClientState handle(ClientChannel channel) {
        System.out.println("Enter your username: ");
        String username = new Scanner(System.in).nextLine();
        System.out.println("Enter your password: ");
        String password = new Scanner(System.in).nextLine();

        Message response;
        try {
            response = channel.authenticate(username, password);
        } catch (Exception e) {
            System.out.println("Failed communicating with the server during authentication");
            return null;
        }

        if (response == null) {
            return null;
        }
        System.out.println(response.getBody());

        if (response.isOk()) {
            new LocalToken(response.getAttribute("sessionToken")).save();
            return new Matchmaking();
        }

        return new Authentication();
    }
}
