package poker.connection.server.queue;

import poker.connection.protocol.Connection;
import poker.connection.protocol.message.Message;

public class Requeuer extends  Thread {
    private final QueueManager queueManager;
    private final Connection connection;

    public Requeuer(QueueManager queueManager, Connection connection) {
        this.queueManager = queueManager;
        this.connection = connection;
    }

    private boolean askPlayerToRequeue() throws InterruptedException {
        Message response = connection.getChannel().sendRequeueRequest("Do you want to requeue?");
        return response.getAttribute("RequeueResponse").equalsIgnoreCase("Yes");
    }

@Override
    public void run() {
        try {
            boolean wantsToRequeue = askPlayerToRequeue();
            if (wantsToRequeue) {
                queueManager.addPlayerToMainQueue(connection);
            }
            queueManager.removePlayerFromRequeue(connection);

            if (queueManager.isRankedMode()) {
                queueManager.removePlayerThreshold(connection);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
