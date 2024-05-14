package poker.connection.protocol.channels;

import com.google.gson.Gson;
import poker.connection.protocol.Channel;
import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.message.Message;
import poker.connection.protocol.message.State;
import poker.game.common.GameState;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static poker.connection.protocol.message.State.*;
import static poker.connection.protocol.message.Status.*;

public class ServerChannel extends Channel {

    Exception exception;

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

    public void sendGameState(GameState gameState) {
        sendMessage(MATCH_DISPLAY, REQUEST, null, Map.of("gameState", new Gson().toJson(gameState)));
    }

    public Message sendRequeueRequest() throws ChannelException {
        sendMessage(REQUEUE, REQUEST, null, null);
        return getResponse(REQUEUE);
    }

    public Message getPlayerMove(String body, GameState gameState, Integer timeout) throws ChannelException {
        sendMessage(MATCH_MOVE, REQUEST, body, Map.of("gameState", new Gson().toJson(gameState)));
        return getResponse(MATCH_MOVE, timeout);
    }

    public Message getRequest(State expectedState) throws ChannelException {
        return getRequest(expectedState, null);
    }

    public Message getRequest(State expectedState, Integer timeout) throws ChannelException {
        return getMessage(expectedState, true, timeout);
    }

    public boolean requestMatchReconnect() throws ChannelException {
        sendMessage(MATCH_RECONNECT, REQUEST, null, null);
        return getResponse(MATCH_RECONNECT).isOk();
    }

    public boolean requestMatchmaking() throws ChannelException {
        sendMessage(MATCHMAKING, REQUEST, null, null);
        return getResponse(MATCHMAKING).isOk();
    }

    public void notifyGameStart() {
        sendMessage(MATCH_START, REQUEST, null, null);
    }

    public void sendTurnTimeout() {
        sendMessage(TURN_TIMEOUT, OK, null, null);
    }
}
