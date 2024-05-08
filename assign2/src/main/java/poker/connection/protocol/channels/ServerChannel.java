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

    public void sendGameState(String body, Map<String, Object> data) {
        sendMessage(MATCH_DISPLAY, REQUEST, body, data);
    }

    public Message sendRequeueRequest(String body) {
        sendMessage(REQUEUE, REQUEST, body, null);
        return getResponse(REQUEUE);
    }

    public Message getPlayerMove(String body) {
        sendMessage(MATCH_MOVE, REQUEST, body, null);
        return getMessage(MATCH_MOVE, false);
    }

    public Message getRequest(State expectedState) {
        return getRequest(expectedState, null);
    }

    public Message getRequest(State expectedState, Integer timeout) {
        return getMessage(expectedState, true, timeout);
    }

    public Message getAuthenticationRequest() {
        return getRequest(AUTHENTICATION);
    }
}
