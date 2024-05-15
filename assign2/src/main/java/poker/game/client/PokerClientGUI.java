package poker.game.client;

import poker.game.common.Card;
import poker.game.common.GamePhase;
import poker.game.common.GameState;
import poker.game.common.PokerPlayer;
import poker.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static poker.game.common.PokerConstants.*;

public class PokerClientGUI {

    public void display(GameState gameState) {
        if (gameState.isGameOver()) {
            displayGameOver(gameState);
        } else if (gameState.isHandOver()) {
            displayHandOver(gameState);
        } else {
            displayGameState(gameState);
        }
    }

    private void displayGameOver(GameState gameState) {
        System.out.println("Game over!");
        ArrayList<PokerPlayer> winners = gameState.getWinners();
        System.out.println("Leaderboard: ");
        for (PokerPlayer winner : winners) {
            System.out.println(winner.getUsername() + " wins with " + winner.getMoney() + " remaining");
        }
    }

    private void displayGameState(GameState gameState) {
        int currentPlayer = gameState.getCurrPlayer();
        int pot = gameState.getPlayers().stream().mapToInt(PokerPlayer::getBet).sum();
        System.out.println("Hand " + (gameState.getHandsPlayed() + 1) + " - " + gameState.getPlayers().get(currentPlayer).getUsername() + "'s turn");
        System.out.println("Pot: " + pot + "\n");
        displayInfo(gameState);
        System.out.println();
    }

    private void displayInfo(GameState gameState) {
        for (int i = 0; i < gameState.getPlayers().size(); i++) {
            if (i != gameState.getPlayer()) {
                if (gameState.getSmallBlind() == i) {
                    System.out.println(gameState.getPlayers().get(i) + " (Small Blind)");
                } else if (gameState.getBigBlind() == i) {
                    System.out.println(gameState.getPlayers().get(i) + " (Big Blind)");
                } else {
                    System.out.println(gameState.getPlayers().get(i));
                }
            }
        }
        System.out.println();
        PokerPlayer player = gameState.getPlayers().get(gameState.getPlayer());

        if (gameState.getPlayer() == gameState.getSmallBlind())
            System.out.println(player + " (Small Blind)");
        else if (gameState.getPlayer() == gameState.getBigBlind())
            System.out.println(player + " (Big Blind)");
        else
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

    private String showCommunityCards(GameState gameState) {
        if (gameState.getPhase() == GamePhase.PREFLOP) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Flop: ");
        for (int i = 0; i < NUM_FLOP_CARDS; i++) {
            sb.append(gameState.getCommunityCards().get(i).toString()).append(" ");
        }
        if (gameState.getPhase() == GamePhase.FLOP) return sb.toString();
        sb.append("\nTurn: ");
        for (int i = NUM_FLOP_CARDS; i < NUM_FLOP_CARDS + NUM_TURN_CARDS; i++) {
            sb.append(gameState.getCommunityCards().get(i).toString()).append(" ");
        }
        if (gameState.getPhase() == GamePhase.TURN) return sb.toString();
        sb.append("\nRiver: ");
        for (int i = NUM_FLOP_CARDS + NUM_TURN_CARDS; i < NUM_COMMUNITY_CARDS; i++) {
            sb.append(gameState.getCommunityCards().get(i).toString()).append(" ");
        }
        return sb.toString();
    }

    private void displayHandOver(GameState gameState) {
        int pot = gameState.getPlayers().stream().mapToInt(PokerPlayer::getBet).sum();
        for (int i = 0; i < gameState.getPlayers().size(); i++) {
            PokerPlayer player = gameState.getPlayers().get(i);
            System.out.println(player);
        }
        System.out.println();
        for (int i = 0; i < gameState.getPlayers().size(); i++) {
            PokerPlayer player = gameState.getPlayers().get(i);
            if (player.getState() != PokerPlayer.PLAYER_STATE.OUT_OF_MONEY || player.getState() != PokerPlayer.PLAYER_STATE.FOLDED)
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

    public Pair<String, Integer> askMove(GameState gameState) {
        StringBuilder options = new StringBuilder();
        Scanner scanner = new Scanner(System.in);
        HashMap<Integer, PokerPlayer.PLAYER_ACTION> actions = new HashMap<Integer, PokerPlayer.PLAYER_ACTION>();
        int option = 1;
        int currBet = gameState.getCurrBet();
        PokerPlayer player = gameState.getPlayers().get(gameState.getPlayer());

        int amount = 0;
        PokerPlayer.PLAYER_ACTION action;

        if (currBet != 0) {
            options.append(option).append(". Fold\n");
            actions.put(option++, PokerPlayer.PLAYER_ACTION.FOLD);
        }

        if (player.getTurnBet() == currBet) {
            options.append(option).append(". Check\n");
            actions.put(option++, PokerPlayer.PLAYER_ACTION.CHECK);
        } else if (player.getTurnBet() < currBet && player.getMoney() > currBet - player.getTurnBet()) {
            options.append(option).append(". Call ("  + (currBet - player.getTurnBet())).append(")\n");
            actions.put(option++, PokerPlayer.PLAYER_ACTION.CALL);
        }

        if (currBet == 0) {
            options.append(option).append(". ").append("Bet").append("\n");
            actions.put(option++, PokerPlayer.PLAYER_ACTION.BET);
        } else if (player.getMoney() > currBet) {
            options.append(option).append(". Raise\n");
            actions.put(option++, PokerPlayer.PLAYER_ACTION.BET);
        }

        options.append(option).append(". All in\n");
        actions.put(option++, PokerPlayer.PLAYER_ACTION.ALL_IN);

        System.out.println(options);
        // Read user input from console
        System.out.println("Enter your choice: ");
        int choice = scanner.nextInt();
        try {
            while (!actions.containsKey(choice)) {
                System.out.println("Invalid choice. Enter your choice: ");
                choice = scanner.nextInt();
            }
        } catch (Exception e) {
            System.out.println("Invalid choice. Enter your choice: ");
            choice = scanner.nextInt();
        }

        action = actions.get(choice);

        if (action == PokerPlayer.PLAYER_ACTION.BET) {
            int minBet = Math.max(currBet - player.getTurnBet(), gameState.getBigBlindBet());
            System.out.println("Enter the amount you want to bet (Minimum bet: " + minBet + ")");
            amount = scanner.nextInt();
            while (amount < minBet) {
                System.out.println("Invalid amount. Enter the amount you want to bet (Minimum bet: " + minBet + "): ");
                amount = scanner.nextInt();
            }
        }

        System.out.println();

        return new Pair<>(action.toString(), amount);
    }
}
