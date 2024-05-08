package poker.connection.protocol.message;

import org.json.JSONObject;

import java.util.Map;

public class Message {
    private final State state;
    private final Status status;
    private final String body;
    private final JSONObject attributes;

    public Message(State state, Status status, String body, Map<String, Object> attributes) {
        this.state = state;
        this.status = status;
        this.body = body;
        this.attributes = new JSONObject(attributes);
    }

    public Message(JSONObject json) {
        this.state = State.valueOf(json.getString("state"));
        this.status = Status.valueOf(json.getString("status"));
        this.body = json.has("body") ? json.getString("body") : null;
        this.attributes = json.getJSONObject("attributes");
    }

    public State getState() {
        return state;
    }

    public Status getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public JSONObject getAttributes() {
        return attributes;
    }

    public String getAttribute(String key) {
        return attributes.getString(key);
    }

    public int getIntAttribute(String key) {
        return attributes.getInt(key);
    }

    public JSONObject getJSONAttribute(String key) {
        return attributes.getJSONObject(key);
    }

    public boolean hasAttribute(String key) {
        return attributes.has(key);
    }

    public boolean isConnectionEndRequest() {
        return isConnectionEnd() && isRequest();
    }

    public boolean isConnectionEnd() {
        return state.equals(State.CONNECTION_END);
    }

    public boolean isOk() {
        return status.equals(Status.OK);
    }

    public boolean isError() {
        return status.equals(Status.ERROR);
    }

    public boolean isRequest() {
        return status.equals(Status.REQUEST);
    }

    public JSONObject toJSON() {
        return new JSONObject()
                .put("state", state)
                .put("status", status)
                .put("body", body)
                .put("attributes", attributes);
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }
}
