package poker.connection.protocol;

import poker.connection.protocol.channels.ServerChannel;

public class Connection {

    private final String username;

    private final String token;

    private final ServerChannel channel;

    public Connection(String username, String token, ServerChannel channel) {
        this.username = username;
        this.token = token;
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public String getSession() {
        return token;
    }

    public ServerChannel getChannel() {
        return channel;
    }
}
