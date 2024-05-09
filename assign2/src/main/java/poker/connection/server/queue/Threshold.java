package poker.connection.server.queue;

public class Threshold {
    private int lowerBound;
    private int upperBound;

    public Threshold(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public int getMidpoint() {
        return (lowerBound + upperBound) / 2;
    }

    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }

    public boolean isWithinThreshold(int value) {
        return value >= lowerBound && value <= upperBound;
    }
}
