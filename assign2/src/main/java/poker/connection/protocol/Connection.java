package poker.connection.protocol;

import poker.connection.protocol.channels.ServerChannel;

public class Connection {
    private final String username;
    private final String token;
    private final ServerChannel channel;
    private int rank;

    public Connection(String username, String token, ServerChannel channel, int rank) {
        this.username = username;
        this.token = token;
        this.channel = channel;
        this.rank = rank;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

}
