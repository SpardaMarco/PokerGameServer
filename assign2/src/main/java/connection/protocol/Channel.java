package connection.protocol;

import java.io.*;
import java.net.Socket;

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

    public void sendMessage(String message) {
        writer.println(message);
    }

    private void sendFlaggedMessage(Flag flag, String... message) {
        if (message.length > 0)
            sendMessage(String.join("\n", message));
        sendMessage(flag.toString());
    }

    public String sendInputRequest(String... message) {
        sendFlaggedMessage(INPUT_REQ, message);
        return getResponse();
    }

    public void sendConnectionEnd(String... message) {
        sendFlaggedMessage(Flag.CONNECTION_END, message);
    }

    public void sendNewConnection() {
        sendFlaggedMessage(Flag.NEW_CONNECTION);
    }

    public void sendNewSession(String sessionToken, String... message) {
        sendFlaggedMessage(Flag.NEW_SESSION, message);
        sendMessage(sessionToken);
    }

    public void sendRecoverSession(String sessionToken) {
        sendFlaggedMessage(Flag.RECOVER_SESSION);
        sendMessage(sessionToken);
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
