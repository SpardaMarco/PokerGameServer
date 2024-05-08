package poker.connection.protocol.channels;

import poker.connection.protocol.Channel;
import poker.connection.protocol.message.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static poker.connection.protocol.message.State.AUTHENTICATION;
import static poker.connection.protocol.message.State.CONNECTION_RECOVERY;
import static poker.connection.protocol.message.Status.REQUEST;

public class ClientChannel extends Channel {
    public ClientChannel(Socket socket) throws IOException {
        super(socket);
    }

    public Message authenticate(String username, String password) {
        sendMessage(AUTHENTICATION, REQUEST, null, Map.of(
                "username", username,
                "password", password)
        );
        return getResponse(AUTHENTICATION);
    }

    public Message recoverSession(String sessionToken) {
        sendMessage(CONNECTION_RECOVERY, REQUEST, null, Map.of(
                "sessionToken", sessionToken)
        );
        return getResponse(CONNECTION_RECOVERY);
    }
}
