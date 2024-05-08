package poker.utils;

public abstract class VirtualThread {

    public void start() {
        Thread.startVirtualThread(this::run);
    }

    protected abstract void run();
}
