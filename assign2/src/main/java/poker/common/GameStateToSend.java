package poker.common;
import java.util.ArrayList;

public class GameStateToSend {
    ArrayList<PokerPlayer> players;
    int player;
    int smallBlind;
    int bigBlind;
    int smallBlindBet;
    int bigBlindBet;
    int handsPlayed;

    public GameStateToSend(ArrayList<PokerPlayer> players, int player, int smallBlind, int bigBlind, int smallBlindBet, int bigBlindBet, int handsPlayed) {
        this.players = players;
        this.player = player;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.smallBlindBet = smallBlindBet;
        this.bigBlindBet = bigBlindBet;
        this.handsPlayed = handsPlayed;
    }

    public ArrayList<PokerPlayer> getPlayers() {
        return this.players;
    }

    public int getPlayer() {
        return this.player;
    }

    public int getSmallBlind() {
        return this.smallBlind;
    }

    public int getBigBlind() {
        return this.bigBlind;
    }

    public int getSmallBlindBet() {
        return this.smallBlindBet;
    }

    public int getBigBlindBet() {
        return this.bigBlindBet;
    }

    public int getHandsPlayed() {
        return this.handsPlayed;
    }
}
