package poker.connection.server.queue;

import poker.connection.protocol.Connection;
import poker.connection.protocol.exceptions.ChannelException;
import poker.connection.protocol.message.Message;

public class Requeuer extends  Thread {
    private final QueueManager queueManager;
    private final Connection connection;

    public Requeuer(QueueManager queueManager, Connection connection) {
        this.queueManager = queueManager;
        this.connection = connection;
    }

    private boolean askPlayerToRequeue() throws InterruptedException {
        Message response;
        try {
            response = connection.getChannel().sendRequeueRequest();
        } catch (ChannelException e) {
            // TODO: handle this exception
            throw new RuntimeException(e);
        }
        return response.getBooleanAttribute("requeue");
    }

@Override
    public void run() {
        try {
            boolean wantsToRequeue = askPlayerToRequeue();
            if (wantsToRequeue) {
                queueManager.addPlayerToMainQueue(connection);
            }
            queueManager.removePlayerFromRequeue(connection);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
