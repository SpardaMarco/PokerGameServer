package connection.server;

import connection.protocol.Channel;

public class Authenticator extends Thread {

    private Channel channel;
    private Server server;
    private DatabaseInterface database;

    public Authenticator(Channel channel, Server server) {
        this.channel = channel;
        this.server = server;
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

        if (database.authenticateUser(username, password)) {
            channel.sendMessage("Welcome admin!");
            server.queuePlayer(username, channel);
            return false;
        } else {
            channel.sendMessage("Invalid credentials. Please try again.");
            return true;
        }
    }
}
