package poker.connection.protocol.channels;

import poker.connection.protocol.Channel;
import poker.connection.protocol.message.Message;
import poker.connection.protocol.message.State;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static poker.connection.protocol.message.State.*;
import static poker.connection.protocol.message.Status.*;

public class ServerChannel extends Channel {
    public ServerChannel(Socket socket) throws IOException {
        super(socket);
    }

    public void acceptConnectionRecovery(String body, String username, String sessionToken) {
        sendMessage(CONNECTION_RECOVERY, OK, body, Map.of(
                "username", username,
                "sessionToken", sessionToken
        ));
    }

    public void rejectConnectionRecovery(String body) {
        sendMessage(CONNECTION_RECOVERY, ERROR, body, null);
    }

    public void acceptAuthentication(String body, String sessionToken) {
        sendMessage(AUTHENTICATION, OK, body, Map.of("sessionToken", sessionToken));
    }

    public void rejectAuthentication(String body) {
        sendMessage(AUTHENTICATION, ERROR, body, null);
    }

    public Message getRequest() {
        return getMessage(null, true);
    }

    public Message getRequest(State expectedState) {
        return getMessage(expectedState, true);
    }

    public Message getAuthenticationRequest() {
        return getRequest(AUTHENTICATION);
    }
}
