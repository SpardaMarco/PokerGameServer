package connection.server;

import connection.protocol.Channel;
import connection.server.database.DatabaseInterface;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

import static connection.protocol.Flag.*;

public class Authenticator extends Thread {

    private final Channel channel;
    private final Server server;
    private final DatabaseInterface database;

    public Authenticator(Channel channel, Server server) throws SQLException {
        this.channel = channel;
        this.server = server;
        this.database = server.getDatabase();
    }

    @Override
    public void run() {

        String incoming = channel.getResponse();
        if (incoming == null) {
            channel.sendConnectionEnd("Connection closed.");
            channel.close();
            return;
        } else if (RECOVER_SESSION.equals(incoming)) {
            handleRecoverSession();
        } else if (NEW_CONNECTION.equals(incoming)) {
            handleNewConnection();
        } else {
            channel.sendConnectionEnd("Invalid request. Closing connection.");
            channel.close();
        }
    }

    private void handleRecoverSession() {
        String token = channel.getResponse();

        String username = database.recoverSession(token);
        if (username != null) {
            channel.sendMessage("Welcome back " + username + "!");
            server.queuePlayer(username, channel);
        } else {
            channel.sendConnectionEnd("Session expired. Closing connection.");
            channel.close();
        }
    }

    private void handleNewConnection(){
        channel.sendMessage("Welcome to PokerLegends!");
        String username = authenticateUser();
        if (username != null) {
            channel.sendMessage("Welcome " + username + "!");
            generateSession(username);
        }
    }

    private String authenticateUser() {

        String username = channel.sendInputRequest(
                "Please enter your username:"
        );
        try {
            if (database.userExists(username)) {
                return loginUser(username);
            } else {
                return registerUser(username);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String registerUser(String username) {
        String password = channel.sendInputRequest(
                "User not found. Setting up new account.",
                "Please enter your password:"
        );
        database.registerUser(username, password);
        channel.sendMessage("Registration successful!");
        return username;
    }

    private String loginUser(String username) {

        channel.sendMessage("User found. Please enter your password.");
        int attempts = 3;
        while (!checkPasssword(username, attempts--))
            if (attempts == 0) {
                channel.sendConnectionEnd("Too many failed attempts. Closing connection.");
                channel.close();
                return null;
            }
        return username;
    }

    private boolean checkPasssword(String username, int attempt) {

        String password = channel.sendInputRequest("Please enter your password:");

        try {
            if (database.authenticateUser(username, password)) {
                channel.sendMessage("Login successful!");
                return true;
            } else {
                channel.sendMessage(String.format(
                        "Invalid credentials. Please try again. (%s attempts remaining)",
                        attempt
                ));
                return false;
            }
        } catch (SQLException e) {
            channel.sendConnectionEnd("An error occurred. Closing connection.");
            channel.close();
            throw new RuntimeException(e);
        }
    }

    private void generateSession(String username) {

        String token = BCrypt.hashpw(username, BCrypt.gensalt());
        long durationSeconds = 24 * 3600;

        if (database.createSession(username, token, durationSeconds)) {
            channel.sendNewSession(token, "Session created successfully!");
        }

        server.queuePlayer(username, channel);
    }
}
