package poker.connection.protocol.channels;

import poker.connection.protocol.Channel;
import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.message.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static poker.connection.protocol.message.State.*;
import static poker.connection.protocol.message.Status.OK;
import static poker.connection.protocol.message.Status.REQUEST;

public class ClientChannel extends Channel {
    public ClientChannel(Socket socket) throws IOException {
        super(socket);
    }

    public Message authenticate(String username, String password) throws ChannelException {
        sendMessage(AUTHENTICATION, REQUEST, null, Map.of(
                "username", username,
                "password", password)
        );
        return getResponse(AUTHENTICATION);
    }

    public Message recoverSession(String sessionToken) throws ChannelException {
        sendMessage(CONNECTION_RECOVERY, REQUEST, null, Map.of(
                "sessionToken", sessionToken)
        );
        return getResponse(CONNECTION_RECOVERY);
    }

    public void handleGameStartRequest() throws ChannelException {
        getRequest(MATCH_START);
    }

    public void sendPlayerMove(String action, String amount) {
        sendMessage(MATCH_MOVE, OK, null, Map.of(
                "action", action,
                "amount", amount)
        );
    }

    public void sendRequeueResponse(boolean requeue) {
        sendMessage(REQUEUE, OK, null, Map.of(
                "requeue", requeue)
        );
    }

    public void acceptMatchmaking() {
        sendMessage(MATCHMAKING, OK, null, null);
    }

    public void acceptMatchReconnect() {
        sendMessage(MATCH_RECONNECT, OK, null, null);
    }
}
