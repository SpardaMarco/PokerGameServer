package poker.connection.server.queue;

import poker.connection.protocol.channels.ServerChannel;

public class Requeuer extends  Thread {
    private final String player;
    private final ServerChannel channel;

    public Requeuer(String player, ServerChannel channel) {
        this.player = player;
        this.channel = channel;
    }

    @Override
    public void run() {
        // TBD: Logic to requeue player
    }
}
