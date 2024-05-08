package poker.game.client;

import java.util.ArrayList;
import poker.game.common.*;

public class PokerClient {
    ArrayList<PokerPlayer> players;
    ArrayList<Card> communityCards;
    int currPlayer;
    int pot;
    int smallBlind;
    int bigBlind;
    int smallBlindBet;
    int bigBlindBet;
    int handsPlayed;

    public PokerClient(OutboundGameState gameState) {
        this.players = gameState.getPlayers();
        this.communityCards = new ArrayList<>();
        this.currPlayer = gameState.getPlayer();
        this.pot = 0;
        this.smallBlind = gameState.getSmallBlind();
        this.bigBlind = gameState.getBigBlind();
        this.smallBlindBet = gameState.getSmallBlindBet();
        this.bigBlindBet = gameState.getBigBlindBet();
        this.handsPlayed = gameState.getHandsPlayed();
    }
}
