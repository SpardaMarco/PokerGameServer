package poker.game.client;

import poker.game.common.Card;
import poker.game.common.GameState;
import poker.game.common.OutboundGameState;
import poker.game.common.PokerPlayer;
import static poker.game.common.PokerConstants.*;

import java.util.ArrayList;

public class PokerClientGui {

    public void display(OutboundGameState gameState) {
        if (gameState.isGameOver()) {
            displayGameOver(gameState);
        } else if (gameState.isHandOver()) {
            displayHandOver(gameState);
        } else {
            displayGameState(gameState);
        }
    }

    private void displayGameOver(OutboundGameState gameState) {
        System.out.println("Game over!");
        ArrayList<PokerPlayer> winners = gameState.getWinners();
        System.out.println("Leaderboard: ");
        for (PokerPlayer winner : winners) {
            System.out.println(winner.getUsername() + " wins with " + winner.getMoney() + " remaining");
        }
    }

    private void displayGameState(OutboundGameState gameState) {
        int currentPlayer = gameState.getCurrPlayer();
        int pot = gameState.getPlayers().stream().mapToInt(PokerPlayer::getBet).sum();
        System.out.println("Hand " + (gameState.getHandsPlayed()+1) + " - " + gameState.getPlayers().get(currentPlayer).getUsername() + "'s turn");
        System.out.println("Pot: " + pot + "\n");
        displayInfo(gameState);
        System.out.println();
    }

    private void displayInfo(OutboundGameState gameState) {
        for (int i = 0; i < gameState.getPlayers().size(); i++) {
            if (i == gameState.getPlayer()) continue;
            else {
                System.out.println(gameState.getPlayers().get(i));
            }
        }
        System.out.println();
        PokerPlayer player = gameState.getPlayers().get(gameState.getPlayer());
        System.out.println(player);
        System.out.println(showPlayerHand(player));
        System.out.println(showCommunityCards(gameState));
    }

    private String showPlayerHand(PokerPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hand: ");
        for (Card card : player.getHand()) {
            sb.append(card.toString()).append(" ");
        }
        if (player.getState() == PokerPlayer.PLAYER_STATE.FOLDED) {
            sb.append(" (FOLDED)");
        }
        return sb.toString();
    }

    private String showCommunityCards(OutboundGameState gameState) {
        if (gameState.getState() == GameState.PREFLOP) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Flop: ");
        for (int i = 0; i < NUM_FLOP_CARDS; i++) {
            sb.append(gameState.getCommunityCards().get(i).toString()).append(" ");
        }
        if (gameState.getState() == GameState.FLOP) return sb.toString();
        sb.append("\nTurn: ");
        for (int i = NUM_FLOP_CARDS; i < NUM_FLOP_CARDS + NUM_TURN_CARDS; i++) {
            sb.append(gameState.getCommunityCards().get(i).toString()).append(" ");
        }
        if (gameState.getState() == GameState.TURN) return sb.toString();
        sb.append("\nRiver: ");
        for (int i = NUM_FLOP_CARDS + NUM_TURN_CARDS; i < NUM_COMMUNITY_CARDS; i++) {
            sb.append(gameState.getCommunityCards().get(i).toString()).append(" ");
        }
        return sb.toString();
    }

    private void displayHandOver(OutboundGameState gameState) {
        int pot = gameState.getPlayers().stream().mapToInt(PokerPlayer::getBet).sum();
        for (int i = 0; i < gameState.getPlayers().size(); i++) {
            PokerPlayer player = gameState.getPlayers().get(i);
            System.out.println(player);
        }
        System.out.println();
        for (int i = 0; i < gameState.getPlayers().size(); i++) {
            PokerPlayer player = gameState.getPlayers().get(i);
            System.out.println(player.getUsername() + "'s hand: " + showPlayerHand(player));
        }
        System.out.println();
        System.out.println(showCommunityCards(gameState));
        ArrayList<PokerPlayer> winners = gameState.getWinners();
        System.out.println("Winners: ");
        for (PokerPlayer winner : winners) {
            System.out.println(winner.getUsername() + " wins " + pot / winners.size() + " with a " + gameState.getHandRanks().get(gameState.getPlayers().indexOf(winner)).toString());
        }
        System.out.println();
    }
}
