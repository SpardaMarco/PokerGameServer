package connection.protocol.message;

public enum Status {
    REQUEST("REQUEST"),
    OK("OK"),
    ERROR("ERROR");

    final String value;

    Status(String value) {
        this.value = value;
    }

    public Boolean equals(String value) {
        return this.value.equals(value);
    }
}
