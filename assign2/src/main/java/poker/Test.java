package poker;

import javax.sound.midi.Soundbank;
import java.util.Scanner;
import java.util.concurrent.*;

public class Test {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Hello, World!");


        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> readInput = executor.submit(scanner::nextLine);

        Thread listener = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("Timeout thread interrupted.");
                return;
            }

            System.out.println("Timeout reached.");
            readInput.cancel(true);
        });

        listener.start();

        String userInput = null;
        try {
            userInput = readInput.get();
            System.out.println("You entered: " + userInput);
        } catch (CancellationException e) {
            System.out.println("Cancelled input.");
        }

        if (userInput != null)
            System.out.println("User input: " + userInput);

        System.out.println("Goodbye, World!");
    }
}
