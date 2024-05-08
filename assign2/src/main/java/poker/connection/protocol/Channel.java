package poker.connection.protocol;

import poker.connection.protocol.message.Message;
import poker.connection.protocol.message.State;
import poker.connection.protocol.message.Status;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Map;

import static poker.connection.protocol.message.State.*;
import static poker.connection.protocol.message.Status.*;

public abstract class Channel {
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    public Channel(Socket socket) throws IOException {
        this.socket = socket;
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        reader = new BufferedReader(new InputStreamReader(input));
        writer = new PrintWriter(output, true);
    }

    protected void sendMessage(Message message) {
        writer.println(message);
    }

    protected void sendMessage(State state, Status status, String body, Map<String, Object> data) {
        sendMessage(new Message(state, status, body, data));
    }

    private Message getMessage() {
        try {
            String line = reader.readLine();
            JSONObject json = new JSONObject(line);
            return new Message(json);
        } catch (IOException e) {
            return null;
        }
    }

    protected Message getMessage(State expectedState, boolean isRequestExpected) {
        Message message = getMessage();

        if (message != null) {
            if (message.isConnectionEndRequest()) {
                this.acceptConnectionEnd();
                System.out.printf("Connection closed by the other party.\nReason: %s\n", message.getBody());
                this.close();
                return null;
            }
            if (expectedState != null && message.getState() != expectedState) {
                throw new RuntimeException("Unexpected state in message:\n" + message);
            }
            if (isRequestExpected == !message.isRequest()) {
                throw new RuntimeException("Expected request but got response:\n" + message);
            } else if (!isRequestExpected && message.isRequest()) {
                throw new RuntimeException("Expected response but got request:\n" + message);
            }
        }
        return message;
    }

    public Message getResponse(State expectedState) {
        return getMessage(expectedState, false);
    }

    public Message requestConnectionEnd(String body) {
        sendMessage(CONNECTION_END, REQUEST, body, null);
        return getResponse(CONNECTION_END);
    }

    protected void acceptConnectionEnd() {
        sendMessage(CONNECTION_END, OK, null, null);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean isOpen() {
        return !socket.isClosed();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
}
