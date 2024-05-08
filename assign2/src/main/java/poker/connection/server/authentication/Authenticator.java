package poker.connection.server.authentication;

import org.mindrot.jbcrypt.BCrypt;
import poker.Server;
import poker.connection.protocol.Connection;
import poker.connection.protocol.channels.ServerChannel;
import poker.connection.protocol.message.Message;
import poker.connection.server.database.DatabaseInterface;
import poker.connection.utils.VirtualThread;

import java.sql.SQLException;

public class Authenticator extends VirtualThread {
    private final Server server;
    private final ServerChannel channel;
    private final DatabaseInterface database;

    private int authenticationAttempts = 3;

    public Authenticator(Server server, ServerChannel channel) throws SQLException {
        this.server = server;
        this.channel = channel;
        this.database = server.getDatabase();
    }

    @Override
    protected void run() {
        handleRequests();
    }

    private void handleRequests() {
        Message request;
        while (channel.isOpen() && (request = channel.getRequest()) != null) {
            switch (request.getState()) {
                case AUTHENTICATION -> handleAuthentication(request);
                case CONNECTION_RECOVERY -> handleRecovery(request);
                case null, default -> {
                    terminateConnection("Invalid request");
                    return;
                }
            }
        }
    }

    private void terminateConnection(String body) {
        channel.requestConnectionEnd(body);
        channel.close();
    }

    private void handleRecovery(Message message) {
        if (!message.hasAttribute("sessionToken")) {
            channel.rejectConnectionRecovery("Missing session token");
            return;
        }

        String token = message.getAttribute("sessionToken");
        String username = database.recoverSession(token);

        if (username != null) {
            String newToken = generateSession(username);
            if (newToken != null)
                channel.acceptConnectionRecovery("Session successfully recovered", username, newToken);
            else
                rejectAuthentication("Something went wrong while generating session");
        } else {
            channel.rejectConnectionRecovery("Invalid or expired session token");
        }
    }

    private void handleAuthentication(Message request) {
        Connection connection = authenticateUser(request);

        if (connection != null)
            server.queuePlayer(connection);
    }

    private Connection authenticateUser(Message request) {
        if (!(request.hasAttribute("username") && request.hasAttribute("password"))) {
            channel.rejectAuthentication("Missing username or password");
            return null;
        }

        String username = request.getAttribute("username");
        String password = request.getAttribute("password");

        try {
            return database.userExists(username) ? loginUser(username, password) : registerUser(username, password);

        } catch (SQLException e) {
            rejectAuthentication("Something went wrong while authenticating user");
            return null;
        }
    }

    private Connection registerUser(String username, String password) throws SQLException {
        if (database.registerUser(username, password))
            return loginUser(username, password);
        else
            rejectAuthentication("Something went wrong while registering user");

        return null;
    }

    private Connection loginUser(String username, String password) throws SQLException {
        if (database.authenticateUser(username, password)) {
            String token = generateSession(username);
            if (token != null) {
                channel.acceptAuthentication("User successfully authenticated", token);
                int rank = database.getUserRank(username);
                return new Connection(username, token, channel, rank);
            } else
                rejectAuthentication("Something went wrong while generating session");
        } else
            rejectAuthentication("Invalid username or password");

        return null;
    }

    private void rejectAuthentication(String body) {
        if (--authenticationAttempts == 0)
            terminateConnection("Too many failed authentication attempts");
        else
            channel.rejectAuthentication(body);
    }

    private String generateSession(String username) {
        String token = BCrypt.hashpw(username, BCrypt.gensalt());
        long durationMillis = 24 * 3600 * 1000;
        if (database.createSession(username, token, durationMillis)) {
            return token;
        }
        return null;
    }
}
