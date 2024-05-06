package poker.common;
import java.util.ArrayList;

public class GameStateToSend {
    ArrayList<PokerPlayer> players;
    ArrayList<PokerPlayer> winners;
    ArrayList<Card> communityCards;
    ArrayList<HandRank> handRanks;
    GameState state;
    boolean isGameOver;
    boolean isHandOver;
    int player;
    int currPlayer;
    int smallBlind;
    int bigBlind;
    int smallBlindBet;
    int bigBlindBet;
    int handsPlayed;

    public GameStateToSend(ArrayList<PokerPlayer> players, ArrayList<PokerPlayer> winners, ArrayList<Card> communityCards, ArrayList<HandRank> handRanks, GameState state, boolean isGameOver, boolean isHandOver, int player, int currPlayer, int smallBlind, int bigBlind, int smallBlindBet, int bigBlindBet, int handsPlayed) {
        this.players = players;
        this.winners = winners;
        this.communityCards = communityCards;
        this.handRanks = handRanks;
        this.state = state;
        this.isGameOver = isGameOver;
        this.isHandOver = isHandOver;
        this.player = player;
        this.currPlayer = currPlayer;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.smallBlindBet = smallBlindBet;
        this.bigBlindBet = bigBlindBet;
        this.handsPlayed = handsPlayed;
    }

    public ArrayList<PokerPlayer> getPlayers() {
        return this.players;
    }

    public ArrayList<PokerPlayer> getWinners() {
        return this.winners;
    }

    public ArrayList<Card> getCommunityCards() {
        return this.communityCards;
    }

    public ArrayList<HandRank> getHandRanks() {
        return this.handRanks;
    }

    public GameState getState() {
        return this.state;
    }

    public boolean IsGameOver() {
        return this.isGameOver;
    }

    public boolean IsHandOver() {
        return this.isHandOver;
    }

    public int getPlayer() {
        return this.player;
    }

    public int getCurrPlayer() {
        return this.currPlayer;
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
