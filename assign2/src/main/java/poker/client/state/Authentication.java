package poker.client.state;

import poker.client.LocalToken;
import poker.connection.protocol.channels.ClientChannel;
import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.exceptions.RequestTimeoutException;
import poker.connection.protocol.message.Message;

import java.util.Scanner;

public class Authentication implements ClientState {

    @Override
    public ClientState handle(ClientChannel channel) {
        System.out.println("Enter your username: ");
        String username = new Scanner(System.in).nextLine().trim();
        System.out.println("Enter your password: ");
        String password = new Scanner(System.in).nextLine().trim();

        Message response;
        try {
            response = channel.authenticate(username, password);
        } catch (ChannelException e) {
            System.out.println("Failed communicating with the server during authentication:\n" + e.getMessage());
            return null;
        }

        System.out.println(response.getBody());

        if (response.isOk()) {
            String sessionToken = response.getAttribute("sessionToken");
            new LocalToken(sessionToken).save();
            channel.setSessionToken(sessionToken);
            return new Matchmaking();
        }

        return new Authentication();
    }
}
