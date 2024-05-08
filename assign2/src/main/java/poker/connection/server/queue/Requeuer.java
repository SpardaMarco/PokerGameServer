package poker.connection.server.queue;

import poker.connection.protocol.channels.ServerChannel;
import poker.connection.protocol.message.Message;

public class Requeuer extends  Thread {
    private final QueueManager queueManager;
    private final String player;
    private final ServerChannel channel;

    public Requeuer(QueueManager queueManager, String player, ServerChannel channel) {
        this.queueManager = queueManager;
        this.player = player;
        this.channel = channel;
    }

    private boolean askPlayerToRequeue() throws InterruptedException {
        Message response = channel.sendRequeueRequest("Do you want to requeue?");
        return response.getAttribute("RequeueResponse").equalsIgnoreCase("Yes");
    }

@Override
    public void run() {
        try {
            boolean wantsToRequeue = askPlayerToRequeue();
            if (wantsToRequeue) queueManager.addPlayerToMainQueue(player, channel);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
