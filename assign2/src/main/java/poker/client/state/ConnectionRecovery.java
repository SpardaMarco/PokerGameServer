package poker.client.state;

import poker.client.LocalToken;
import poker.connection.protocol.channels.ClientChannel;
import poker.connection.protocol.message.Message;

import java.util.Scanner;

public class ConnectionRecovery implements ClientState {

    @Override
    public ClientState handle(ClientChannel channel) {
        LocalToken token = LocalToken.retrieve();

        if (token != null) {
            if (confirmRecovery()) {
                Message response = channel.recoverSession(token.toString());
                if (response == null) {
                    return null;
                }
                System.out.println(response.getBody());
                if (response.isOk()) {
                    new LocalToken((response.getAttribute("sessionToken"))).save();
                    return new Matchmaking();
                }
            }
        }

        return new Authentication();
    }

    private boolean confirmRecovery() {
        System.out.println("Do you wish to recover your previous session? (Y/N)");
        String input = new Scanner(System.in).nextLine();
        return input.equalsIgnoreCase("Y");
    }
}
