package poker.connection.protocol.message;

public enum State {
    NEW_CONNECTION("NEW_CONNECTION"),
    CONNECTION_RECOVERY("CONNECTION_RECOVERY"),
    AUTHENTICATION("AUTHENTICATION"),
    CONNECTION_END("CONNECTION_END"),
    MATCHMAKING("MATCHMAKING"),
    MATCH("MATCH");

    final String value;

    State(String value) {
        this.value = value;
    }

    public Boolean equals(String value) {
        return this.value.equals(value);
    }

    public String toString() {
        return this.value;
    }
}
