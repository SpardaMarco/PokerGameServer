package poker.common;
public class Card {
    public static enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    public static enum Rank {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
    }

    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public String toString() {
        return "| " + rank + " of " + suit + " |";
    }

    public boolean equals(Card other) {
        return suit == other.suit && rank == other.rank;
    }

    public boolean isSameRank(Card other) {
        return rank == other.rank;
    }

    public boolean isSameSuit(Card other) {
        return suit == other.suit;
    }

    public int compareTo(Card other) {
        return rank.compareTo(other.rank);
    }
}
