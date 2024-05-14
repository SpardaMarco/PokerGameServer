package poker.client.state;

import poker.client.LocalToken;
import poker.connection.protocol.channels.ClientChannel;
import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.exceptions.RequestTimeoutException;
import poker.connection.protocol.message.Message;

import java.util.Scanner;

import static poker.connection.protocol.message.State.MATCHMAKING;
import static poker.connection.protocol.message.State.MATCH_RECONNECT;

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
            try {
                Message message = channel.getRequest();
                if (MATCHMAKING.equals(message.getState())) {
                    channel.acceptMatchmaking();
                    return new Matchmaking();
                } else if (MATCH_RECONNECT.equals(message.getState())) {
                    channel.acceptMatchReconnect();
                    return new Match();
                } else {
                    System.out.println("Unexpected message received after authentication: " + message);
                    return null;
                }
            } catch (ChannelException e) {
                System.out.println("Failed communicating with the server after authentication:\n" + e.getMessage());
                return null;
            }
        }

        return new Authentication();
    }
}
