package poker.connection.protocol.message;

public enum State {
    NEW_CONNECTION("NEW_CONNECTION"),
    CONNECTION_RECOVERY("CONNECTION_RECOVERY"),
    AUTHENTICATION("AUTHENTICATION"),
    CONNECTION_END("CONNECTION_END"),
    MATCHMAKING("MATCHMAKING"),
    MATCH_START("MATCH_START"),
    MATCH_DISPLAY("MATCH_DISPLAY"),
    MATCH_MOVE("MATCH_MOVE"),
    MATCH_END("MATCH_END"),
    REQUEUE("REQUEUE");

    final String value;

    State(String value) {
        this.value = value;
    }

    public Boolean equals(String value) {
        return this.value.equals(value);
    }

    public Boolean equals(State state) {
        return this.value.equals(state.value);
    }

    public String toString() {
        return this.value;
    }
}
