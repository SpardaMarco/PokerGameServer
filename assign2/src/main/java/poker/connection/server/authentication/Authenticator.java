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
        while (channel.isOpen()) {
            request = channel.getRequest(20);
            if (request == null) {
                terminateConnection("Authentication timed out");
                return;
            }
            switch (request.getState()) {
                case AUTHENTICATION -> { if (handleAuthentication(request)) return; }
                case CONNECTION_RECOVERY -> { if (handleRecovery(request)) return; }
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

    private boolean handleRecovery(Message message) {
        Connection connection = recoverSession(message);

        if (connection != null) {
            server.queuePlayer(connection);
            return true;
        }
        return false;
    }

    private Connection recoverSession(Message message) {
        if (!message.hasAttribute("sessionToken")) {
            channel.rejectConnectionRecovery("Missing session token");
            return null;
        }

        String token = message.getAttribute("sessionToken");
        String username = database.recoverSession(token);

        if (username != null) {
            String newToken = generateSession(username);
            if (newToken != null) {
                channel.acceptConnectionRecovery("Session successfully recovered", username, newToken);
                return new Connection(username, newToken, channel, database.getUserRank(username));
            }
            else
                rejectAuthentication("Something went wrong while generating session");
        } else {
            channel.rejectConnectionRecovery("Invalid or expired session token");
        }

        return null;
    }

    private boolean handleAuthentication(Message request) {
        Connection connection = authenticateUser(request);

        if (connection != null) {
            server.queuePlayer(connection);
            return true;
        }
        return false;
    }

    private Connection authenticateUser(Message request) {
        if (!(request.hasAttribute("username") && request.hasAttribute("password"))) {
            channel.rejectAuthentication("Missing username or password");
            return null;
        }

        String username = request.getAttribute("username");
        String password = request.getAttribute("password");

        try {
            return database.userExists(username) ? handleLogin(username, password) : handleRegistration(username, password);

        } catch (SQLException e) {
            rejectAuthentication("Something went wrong while authenticating user");
            return null;
        }
    }

    private Connection handleRegistration(String username, String password) throws SQLException {
        if (database.registerUser(username, password)) {
            Connection connection = login(username, password);
            if (connection != null) {
                channel.acceptAuthentication("User successfully registered", connection.getSession());
                return connection;
            }
        }
        else
            rejectAuthentication("Something went wrong while registering user");
        return null;
    }

    private Connection handleLogin(String username, String password) throws SQLException {
        Connection connection = login(username, password);
        if (connection != null) {
            channel.acceptAuthentication("User successfully logged in", connection.getSession());
            return connection;
        }
        return null;
    }

    private Connection login(String username, String password) throws SQLException {
        if (database.authenticateUser(username, password)) {
            String token = generateSession(username);
            if (token != null)
                return new Connection(username, token, channel, database.getUserRank(username));
            else
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
