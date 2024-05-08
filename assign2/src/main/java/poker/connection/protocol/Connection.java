package poker.connection.protocol;

public class Connection {
    private final String username;
    private final String token;
    private final Channel channel;
    private int rank;

    public Connection(String username, String token, Channel channel, int rank) {
        this.username = username;
        this.token = token;
        this.channel = channel;
        this.rank = rank;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

}
