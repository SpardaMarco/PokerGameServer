package poker.connection.server.queue;

import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.message.Message;

public class Requeuer extends  Thread {
    private final Queuer queuer;
    private final Connection connection;

    public Requeuer(Queuer queuer, Connection connection) {
        this.queuer = queuer;
        this.connection = connection;
    }

    private boolean askPlayerToRequeue() throws InterruptedException {
        Message response;
        try {
            response = connection.getChannel().sendRequeueRequest();
        } catch (ChannelException e) {
            return false;
        }
        return response.getBooleanAttribute("requeue");
    }

@Override
    public void run() {
        try {
            boolean wantsToRequeue = askPlayerToRequeue();
            if (wantsToRequeue) {
                queuer.queuePlayer(connection);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
