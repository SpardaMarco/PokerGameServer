package poker.server;
import poker.common.*;
import java.util.*;
public class Poker {
    private final int HAND_SIZE = 2;
    private final int NUM_PLAYERS = 6;
    private final int NUM_FLOP_CARDS = 3;
    private final int NUM_TURN_CARDS = 1;
    private final int NUM_RIVER_CARDS = 1;
    private final int NUM_COMMUNITY_CARDS = NUM_FLOP_CARDS + NUM_TURN_CARDS + NUM_RIVER_CARDS;
    private final int HANDS_PER_BLIND = 5;
    private final int BLIND_INCREASE = 2;
    private final int STARTING_MONEY = 10000;
    private final int MAX_NUM_HANDS = 20;
    private int handsPlayed;
    private int smallBlindBet;
    private int bigBlindBet;
    private int smallBlind;
    private int bigBlind;
    private int lastRaiser;
    private int currPlayer;
    private int pot;
    private int currBet;
    private boolean isHandOver = false;
    private boolean isGameOver = false;
    private GameState state;

    private final ArrayList<PokerPlayer> players = new ArrayList<>(NUM_PLAYERS);
    private final Deck deck = new Deck();
    private final ArrayList<Card> communityCards = new ArrayList<>(NUM_COMMUNITY_CARDS);
    private final HandAnalyzer handAnalyzer = new HandAnalyzer(this);

    public Poker(ArrayList<String> players) {
        this.handsPlayed = 0;
        this.smallBlindBet = 50;
        this.bigBlindBet = 100;
        this.smallBlind = 0;
        this.bigBlind = 1;
        this.currPlayer = 0;
        this.state = GameState.PREFLOP;

        for (String player : players) {
            this.players.add(new PokerPlayer(player, STARTING_MONEY));
        }

        this.startHand();
    }

    public ArrayList<Card> getCommunityCards() {
        return this.communityCards;
    }

    public ArrayList<PokerPlayer> getPlayers() {
        return this.players;
    }

    public ArrayList<PokerPlayer> getActivePlayers() {
        ArrayList<PokerPlayer> activePlayers = new ArrayList<>();
        for (PokerPlayer player : players) {
            if (player.getState() != PokerPlayer.PLAYER_STATE.FOLDED && player.getState() != PokerPlayer.PLAYER_STATE.OUT_OF_MONEY) {
                activePlayers.add(player);
            }
        }
        return activePlayers;
    }

    public int getCurrPlayer() {
        return this.currPlayer;
    }

    public int getCurrBet() {
        return this.currBet;
    }

    public boolean getIsHandOver() {
        return this.isHandOver;
    }

    public boolean getIsGameOver() {
        return this.isGameOver;
    }

    public int getPot() {
        return this.pot;
    }

    public int getHandsPlayed() {
        return this.handsPlayed;
    }

    public HandAnalyzer getHandAnalyzer() {
        return this.handAnalyzer;
    }

    private boolean isPlayerSmallBlind(PokerPlayer player) {
        return player.getUsername().equals(this.players.get(smallBlind).getUsername());
    }

    private boolean isPlayerBigBlind(PokerPlayer player) {
        return player.getUsername().equals(this.players.get(bigBlind).getUsername());
    }

    private int getNextActivePlayer(int playerIndex) {
        int nextPlayer = (playerIndex + 1) % NUM_PLAYERS;
        while (this.players.get(nextPlayer).getState() == PokerPlayer.PLAYER_STATE.FOLDED || this.players.get(nextPlayer).getState() == PokerPlayer.PLAYER_STATE.ALL_IN || this.players.get(nextPlayer).getState() == PokerPlayer.PLAYER_STATE.OUT_OF_MONEY) {
            nextPlayer = (nextPlayer + 1) % NUM_PLAYERS;
        }
        return nextPlayer;
    }

    private void updateBlinds() {
        this.smallBlind = getNextActivePlayer(this.smallBlind);
        this.bigBlind = getNextActivePlayer(this.bigBlind);

        if ((this.handsPlayed % HANDS_PER_BLIND) == 0) {
            this.smallBlindBet *= BLIND_INCREASE;
            this.bigBlindBet *= BLIND_INCREASE;
        }
    }

    private boolean isHandOver() {
        if ((this.state == GameState.RIVER) && (this.currPlayer == this.lastRaiser)) return true;
        if (this.getActivePlayers().size() == 1) return true;

        int numPlayersNotAllIn = 0;
        for (PokerPlayer player : getActivePlayers()) {
            if (player.getState() != PokerPlayer.PLAYER_STATE.ALL_IN) {
                numPlayersNotAllIn++;
            }
        }

        return numPlayersNotAllIn == 0;
    }

    private boolean isGameOver() {
        if (this.handsPlayed == MAX_NUM_HANDS) return true;

        int numPlayersWithMoney = 0;
        for (PokerPlayer player : players) {
            if (player.getMoney() > 0) {
                numPlayersWithMoney++;
            }
        }

        return numPlayersWithMoney == 1;
    }

    public ArrayList<PokerPlayer> getGameWinners() {
        ArrayList<PokerPlayer> winners = new ArrayList<PokerPlayer>(players);
        winners.sort((PokerPlayer p1, PokerPlayer p2) -> p1.getMoney() - p2.getMoney());
        return winners;
    }

    public ArrayList<PokerPlayer> getHandWinners() {
        return handAnalyzer.getWinners();
    }

    private void startHand() {
        this.pot = 0;
        this.lastRaiser = -1;
        this.currBet = 0;
        this.currPlayer = this.smallBlind;
        this.state = GameState.PREFLOP;
        this.isHandOver = false;
        this.isGameOver = false;

        deck.reset();
        deck.shuffle();
        for (PokerPlayer player : players) {
            player.setHand(deck.dealCards(HAND_SIZE));
        }
        this.communityCards.clear();
        this.communityCards.addAll(deck.dealCards(NUM_COMMUNITY_CARDS));
        this.takeAction(PokerPlayer.PLAYER_ACTION.BET, this.smallBlindBet);
        this.takeAction(PokerPlayer.PLAYER_ACTION.BET, this.bigBlindBet);
    }

    private void nextHand() {
        this.handsPlayed++;
        for (PokerPlayer player : players) {
            player.resetBet();
            player.resetState();
        }
        this.updateBlinds();
        this.startHand();
    }

    public void endHand() {
        ArrayList<PokerPlayer> winners = this.getHandWinners();
        int numWinners = winners.size();
        int winnings = this.pot / numWinners;
        for (PokerPlayer winner : winners) {
            winner.addMoney(winnings);
        }
        if (this.isGameOver()) {
            this.isGameOver = true;
        } else {
            this.nextHand();
        }
    }

    public void nextTurn() {
        switch (this.state) {
            case PREFLOP:
                this.state = GameState.FLOP;
                break;
            case FLOP:
                this.state = GameState.TURN;
                break;
            case TURN:
                this.state = GameState.RIVER;
                break;
        }
        this.currPlayer = this.smallBlind;
        this.lastRaiser = this.smallBlind;
    }

    public void nextPlayer() {
        this.currPlayer = (this.currPlayer + 1) % NUM_PLAYERS;
    }

    private void afterPlayerAction() {
        this.nextPlayer();
        if (this.isHandOver()) {
            this.isHandOver = true;
            this.state = GameState.RIVER;
        } else if (this.currPlayer == this.lastRaiser) {
            this.nextTurn();
        } else if (this.players.get(this.currPlayer).getState() == PokerPlayer.PLAYER_STATE.FOLDED || this.players.get(this.currPlayer).getState() == PokerPlayer.PLAYER_STATE.ALL_IN || this.players.get(this.currPlayer).getState() == PokerPlayer.PLAYER_STATE.OUT_OF_MONEY) {
            this.afterPlayerAction();
        }
    }

    public void takeAction(PokerPlayer.PLAYER_ACTION action, int amount) {
        if (this.isHandOver || this.isGameOver) return;
        PokerPlayer player = this.players.get(this.currPlayer);
        int playerBet = player.getBet();
        switch (action) {
            case FOLD:
                player.fold();
                break;
            case BET:
                player.placeBet(amount);
                if (player.getBet() > this.currBet) {
                    this.currBet = player.getBet();
                    this.lastRaiser = this.currPlayer;
                }
                break;
            case CALL:
                player.placeBet(this.currBet - player.getBet());
                break;
            case CHECK:
                break;
            case ALL_IN:
                player.placeBet(player.getMoney());
                if (player.getBet() > this.currBet) {
                    this.currBet = player.getBet();
                    this.lastRaiser = this.currPlayer;
                }
                break;
        }

        this.pot += player.getBet() - playerBet;

        // After player action logic
        this.afterPlayerAction();
    }

    public String showPlayerHand(PokerPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hand: ");
        for (Card card : player.getHand()) {
            sb.append(card.toString() + " ");
        }
        if (player.getState() == PokerPlayer.PLAYER_STATE.FOLDED) {
            sb.append(" (FOLDED)");
        }
        return sb.toString();
    }

    public String showCommunityCards() {
        if (this.state == GameState.PREFLOP) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Flop: ");
        for (int i = 0; i < NUM_FLOP_CARDS; i++) {
            sb.append(this.communityCards.get(i).toString()).append(" ");
        }
        if (this.state == GameState.FLOP) return sb.toString();
        sb.append("\nTurn: ");
        for (int i = NUM_FLOP_CARDS; i < NUM_FLOP_CARDS + NUM_TURN_CARDS; i++) {
            sb.append(this.communityCards.get(i).toString()).append(" ");
        }
        if (this.state == GameState.TURN) return sb.toString();
        sb.append("\nRiver: ");
        for (int i = NUM_FLOP_CARDS + NUM_TURN_CARDS; i < NUM_COMMUNITY_CARDS; i++) {
            sb.append(this.communityCards.get(i).toString()).append(" ");
        }
        return sb.toString();
    }

    public String showPlayerInfo(PokerPlayer player) {
        return player.toString();
    }
}

