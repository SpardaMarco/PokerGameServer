package connection.protocol;

public enum Flag {
    INPUT_REQ("INPUT_REQ"),
    END_CONNECTION("CONNECTION_END"),
    NEW_CONNECTION("NEW_CONNECTION"),
    NEW_SESSION("NEW_SESSION"),
    RECOVER_SESSION("RECOVER_SESSION");
    private final String value;
    Flag(String value) {
        this.value = value;
    }

    public Boolean equals(String value) {
        return this.value.equals(value);
    }

    public String toString() {
        return this.value;
    }
}
