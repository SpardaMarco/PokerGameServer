package connection.server;

import connection.protocol.Channel;

import java.awt.image.ConvolveOp;
import java.sql.SQLException;

public class Authenticator extends Thread {

    private Channel channel;
    private Server server;
    private DatabaseInterface database;

    public Authenticator(Channel channel, Server server) throws SQLException {
        this.channel = channel;
        this.server = server;
        this.database = server.getDatabase();
    }

    @Override
    public void run() {

        int attempts = 0;
        while (authenticateUser())
            if (++attempts == 3) {
                channel.sendConnectionEnd("Too many failed attempts. Closing connection.");
                channel.close();
                return;
            }
    }

    private boolean authenticateUser() {
        String username = channel.sendInputRequest(
                "Welcome to Poker!",
                "Please enter your username:"
        );

        String password = channel.sendInputRequest("Please enter your password:");

        try {
            if (database.authenticateUser(username, password)) {
                channel.sendMessage("Welcome admin!");
                server.queuePlayer(username, channel);
                return false;
            } else {
                channel.sendMessage("Invalid credentials. Please try again.");
                return true;
            }
        } catch (SQLException e) {
            channel.sendConnectionEnd("An error occurred. Closing connection.");
            channel.close();
            throw new RuntimeException(e);
        }
    }
}
