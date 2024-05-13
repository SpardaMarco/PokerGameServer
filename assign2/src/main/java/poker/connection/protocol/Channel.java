package poker.connection.protocol;

import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.exceptions.ClosedConnectionException;
import poker.connection.protocol.exceptions.TokenMismatchException;
import poker.connection.protocol.exceptions.UnexpectedMessageException;
import poker.connection.protocol.exceptions.RequestTimeoutException;
import poker.connection.protocol.message.Message;
import poker.connection.protocol.message.State;
import poker.connection.protocol.message.Status;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

import static poker.connection.protocol.message.State.*;
import static poker.connection.protocol.message.Status.*;

public abstract class Channel {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private String sessionToken;
    private Exception exception;

    public Channel(Socket socket) throws IOException {
        this.socket = socket;
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        reader = new BufferedReader(new InputStreamReader(input));
        writer = new PrintWriter(output, true);
        sessionToken = null;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    protected void sendMessage(Message message) {
        writer.println(message);
    }

    protected void sendMessage(State state, Status status, String body, Map<String, Object> data) {
        sendMessage(new Message(state, status, body, data, sessionToken));
    }

    private Message getMessage() {

        try  {
            String line = reader.readLine();
            JSONObject json = new JSONObject(line);
            return new Message(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Message getMessage(int timeout) throws ChannelException {

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {

            Future<String> future = executor.submit(reader::readLine);
            String line;

            try {
                line = future.get(timeout, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new RequestTimeoutException("Timeout while waiting for message");
            }
            JSONObject json = new JSONObject(line);
            return new Message(json);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected Message getMessage(State expectedState, boolean isRequestExpected, Integer timeout) throws ChannelException {

        if (isClosed()) {
            throw new ClosedConnectionException("Connection is closed");
        }

        Message message = (timeout != null) ? getMessage(timeout) : getMessage();

        if (!message.matchesSessionToken(sessionToken)) {
            throw new TokenMismatchException(String.format(
                    "Expected session token %s but got %s:\n%s",
                    sessionToken, message.getAttribute("sessionToken"), message)
            );
        }
        if (message.isConnectionEndRequest()) {
            throw new ClosedConnectionException(String.format(
                    "Connection closed by the other party: %s", message.getBody())
            );
        }
        if (message.isConnectionCheckRequest()){
            acceptConnectionCheck();
            return getMessage(expectedState, isRequestExpected, timeout);
        }
        if (expectedState != null && message.getState() != expectedState) {
            throw new UnexpectedMessageException(String.format(
                    "Expected state %s but got %s:\n%s",
                    expectedState, message.getState(), message)
            );
        }
        if (isRequestExpected && !message.isRequest()) {
            throw new UnexpectedMessageException("Expected request but got response:\n" + message);
        } else if (!isRequestExpected && message.isRequest()) {
            throw new UnexpectedMessageException("Expected response but got request:\n" + message);
        }
        return message;
    }

    public Message getResponse(State expectedState) throws ChannelException {
        return getMessage(expectedState, false, null);
    }

    public Message getResponse(State expectedState, Integer timeout) throws ChannelException {
        return getMessage(expectedState, false, timeout);
    }

    public Message getRequest() throws ChannelException {
        return getMessage(null, true, null);
    }

    public Message getRequest(Integer timeout) throws ChannelException {
        return getMessage(null, true, timeout);
    }

    public Message getRequest(State expectedState) throws ChannelException {
        return getMessage(expectedState, true, null);
    }

    public Message getRequest(State expectedState, Integer timeout) throws ChannelException {
        return getMessage(expectedState, true, timeout);
    }

    public void requestConnectionEnd(String body) {
        sendMessage(CONNECTION_END, REQUEST, body, null);
    }

    protected void acceptConnectionEnd() {
        sendMessage(CONNECTION_END, OK, null, null);
    }

    private Message requestConnectionCheck() throws ChannelException {
        sendMessage(CONNECTION_CHECK, REQUEST, null, null);
        return getResponse(CONNECTION_CHECK, 3);
    }

    private void acceptConnectionCheck() {
        sendMessage(CONNECTION_CHECK, OK, null, null);
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

    public boolean isAlive() {
        Message response;
        try {
            response = requestConnectionCheck();
        } catch (ChannelException e) {
            return false;
        }
        return response != null && response.isOk();
    }

    public boolean isBroken() {
        return !isAlive();
    }
}
