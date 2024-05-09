package poker.game.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import poker.game.common.*;
import poker.game.server.*;

// Only for testing purposes
public class Test {
    Poker poker;
    PokerClientGui displayer = new PokerClientGui();
    ArrayList<String> players = new ArrayList<String>();

    public Test() {
        players.add("Alice");
        players.add("Bob");
        players.add("Charlie");
        players.add("David");
        players.add("Eve");
        players.add("Frank");
        poker = new Poker(players);
        play();
    }

    public void play() {
        while (!poker.getIsGameOver()) {
            while(!poker.getIsHandOver()) {
                int currentPlayer = poker.getCurrPlayer();
                GameState g = poker.getGameStateToSend(currentPlayer);
                displayer.display(g);
                makePlay(currentPlayer);
                currentPlayer = poker.getCurrPlayer();
            }
            int currentPlayer = poker.getCurrPlayer();
            GameState g = poker.getGameStateToSend(currentPlayer);
            displayer.display(g);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            poker.endHand();
        }

        GameState g = poker.getGameStateToSend(poker.getCurrPlayer());
        displayer.display(g);
    }

    private void makePlay(int currentPlayer) {
        StringBuilder options = new StringBuilder();
        Scanner scanner = new Scanner(System.in);
        HashMap<Integer, PokerPlayer.PLAYER_ACTION> actions = new HashMap<Integer, PokerPlayer.PLAYER_ACTION>();
        int option = 1;

        PokerPlayer player = poker.getPlayers().get(currentPlayer);
        options.append(option).append(". Fold\n");
        actions.put(option++, PokerPlayer.PLAYER_ACTION.FOLD);

        if (player.getBet() == poker.getCurrBet()) {
            options.append(option).append(". Check\n");
            actions.put(option++, PokerPlayer.PLAYER_ACTION.CHECK);
        }
        else if (player.getBet() < poker.getCurrBet() && player.getMoney() > poker.getCurrBet() - player.getBet()) {
            options.append(option).append(". Call\n");
            actions.put(option++, PokerPlayer.PLAYER_ACTION.CALL);
        }

        options.append(option).append(". ").append(player.getBet() == 0 ? "Bet" : "Raise").append("\n");
        actions.put(option++, PokerPlayer.PLAYER_ACTION.BET);

        options.append(option).append(". All in\n");
        actions.put(option++, PokerPlayer.PLAYER_ACTION.ALL_IN);

        System.out.println(options.toString());
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
        PokerPlayer.PLAYER_ACTION action = actions.get(choice);
        if (action == PokerPlayer.PLAYER_ACTION.BET) {
            System.out.println("Enter the amount you want to bet (Minimum bet: " + Math.max(poker.getCurrBet()-player.getBet(), 1) + ")");
            int amount = scanner.nextInt();
            while (amount < poker.getCurrBet()) {
                System.out.println("Invalid amount. Enter the amount you want to bet (Minimum bet: " + poker.getCurrBet() + "): ");
                amount = scanner.nextInt();
            }
            poker.takeAction(action, amount);
        }
        else {
            poker.takeAction(action, 0);
        }

        System.out.println();
    }

    public static void main(String[] args) {
        new Test();
    }
}
