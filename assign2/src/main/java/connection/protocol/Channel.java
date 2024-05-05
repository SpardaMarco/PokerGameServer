package connection.protocol;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

import static connection.protocol.Flag.INPUT_REQ;

public class Channel {

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

    public String getResponse() {

        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(JSONObject json) {
        writer.println(json.toString());
    }

    private void sendFlaggedMessage(Flag flag) {
        if (message.length > 0)
            sendMessage(String.join("\n", message));
        sendMessage(flag.toString());
    }

    public String sendInputRequest(String... message) {
        sendFlaggedMessage(INPUT_REQ, message);
        return getResponse();
    }

    public void sendEndConnection(String... message) {
        sendFlaggedMessage(Flag.END_CONNECTION, message);
    }

    public void sendNewConnection() {
        sendFlaggedMessage(Flag.NEW_CONNECTION);
    }

    public void sendNewSession(String sessionToken) {
        JSONObject json = new JSONObject();
        json.put("flag", Flag.NEW_SESSION);
        json.put("token", sessionToken);
        sendMessage(json);
    }

    public void sendRecoverSession(String sessionToken) {
        JSONObject json = new JSONObject();
        json.put("flag", Flag.RECOVER_SESSION);
        json.put("sessionToken", sessionToken);
        sendMessage(json);
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
