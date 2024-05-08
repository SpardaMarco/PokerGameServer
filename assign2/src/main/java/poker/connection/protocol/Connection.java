package poker.connection.protocol;

public class Connection {

    private final String username;

    private final String token;

    private final Channel channel;

    public Connection(String username, String token, Channel channel) {
        this.username = username;
        this.token = token;
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public Channel getChannel() {
        return channel;
    }
}
