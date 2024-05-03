package connection.protocol;

public enum Flag {
    INPUT_REQ("INPUT_REQ"),
    CONNECTION_END("CONNECTION_END");
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
