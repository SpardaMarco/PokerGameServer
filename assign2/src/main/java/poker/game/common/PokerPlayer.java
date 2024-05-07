package poker.game.common;
import java.util.ArrayList;
public class PokerPlayer {
    private final String username;
    private int money;
    private int bet;
    private ArrayList<Card> hand;
    private PLAYER_STATE state;
    public static enum PLAYER_STATE {
        FOLDED, BETTING, ALL_IN, WAITING, PASS, OUT_OF_MONEY
    }
    public static enum PLAYER_ACTION {
        FOLD, CHECK, BET, CALL, ALL_IN
    }

    public PokerPlayer(String name, int money) {
        this.username = name;
        this.money = money;
        this.hand = new ArrayList<Card>(2);
        this.state = PLAYER_STATE.WAITING;
        this.bet = 0;
    }

    public PokerPlayer(String name, int money, PLAYER_STATE state, int bet) {
        this.username = name;
        this.money = money;
        this.hand = new ArrayList<Card>(2);
        this.state = state;
        this.bet = bet;
    }


    public String getUsername() {
        return username;
    }

    public int getMoney() {
        return money;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public PLAYER_STATE getState() {
        return state;
    }

    public int getBet() {
        return bet;
    }

    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
    }

    public void addMoney(int amount) {
        this.money += amount;
    }

    public void placeBet(int amount) {
        if (this.money <= amount) {
            this.bet += this.money;
            this.money = 0;
            this.state = PLAYER_STATE.ALL_IN;
        }
        else {
            this.bet += amount;
            this.money -= amount;
            this.state = PLAYER_STATE.BETTING;
        }
    }

    public void pass() {
        this.state = PLAYER_STATE.PASS;
    }

    public void resetBet() {
        this.bet = 0;
    }
    public void fold() {
        this.state = PLAYER_STATE.FOLDED;
    }

    public void resetState() {
        this.state = PLAYER_STATE.WAITING;
        if (this.money == 0) {
            this.state = PLAYER_STATE.OUT_OF_MONEY;
        }
    }

    public String toString() {
        return username + ": " + money + " | " + state + " | " + bet;
    }
}
