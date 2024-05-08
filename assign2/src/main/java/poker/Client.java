package poker;

import poker.connection.client.ClientChannelFactory;
import poker.connection.protocol.channels.ClientChannel;
import poker.connection.protocol.message.Message;
import poker.connection.protocol.message.State;

import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.util.Scanner;

import static poker.connection.protocol.message.State.*;

public class Client {
    private final ClientChannel channel;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            new Client(host, port).init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Client(String host, int port) throws Exception {
        channel = new ClientChannelFactory().createChannel(host, port);
    }

    private void init() {
        State state = CONNECTION_RECOVERY;

        while (true) {
            switch (state) {
                case CONNECTION_RECOVERY:{
                    state = handleConnectionRecovery();
                    break;
                }
                case AUTHENTICATION:{
                    state = handleAuthentication(state);
                    break;
                }
                case MATCHMAKING: {
                    state = handleQueue(state);
                    break;
                }
                case CONNECTION_END: {
                    handleConnectionEnd();
                    return;
                }
            }
        }
    }

    private State handleConnectionRecovery() {
        String sessionToken = getSessionToken();

        if (sessionToken != null) {
            System.out.println("Do you wish to recover your previous session? (Y/N)");
            String input = new Scanner(System.in).nextLine();

            if (input.equalsIgnoreCase("Y")) {
                Message response = channel.recoverSession(sessionToken);
                if (response == null) {
                    return CONNECTION_END;
                }
                System.out.println(response.getBody());
                if (response.isOk()) {
                    saveSessionToken(response.getAttribute("sessionToken"));
                    return MATCHMAKING;
                }
            }
        }

        return AUTHENTICATION;
    }

    private State handleAuthentication(State state) {
        System.out.println("Enter your username: ");
        String username = new Scanner(System.in).nextLine();
        System.out.println("Enter your password: ");
        String password = new Scanner(System.in).nextLine();

        Message response = channel.authenticate(username, password);
        if (response == null) {
            return CONNECTION_END;
        }
        System.out.println(response.getBody());

        if (response.isOk()) {
            saveSessionToken(response.getAttribute("sessionToken"));
            return MATCHMAKING;
        }

        return AUTHENTICATION;
    }

    // TODO
    private State handleQueue(State state) {
        System.out.print("Waiting in queue");
        for (int i = 0; i < 3; i++) {
            System.out.print(".");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println();
        Message response = channel.requestConnectionEnd("Connection terminated by client");
        if (response.isOk()) {
            System.out.println("Connection successfully terminated");
        } else {
            System.out.println("Something went wrong while terminating connection");
        }

        return CONNECTION_END;
    }

    private void handleConnectionEnd() {
        System.out.println("Connection ended");
        channel.close();
    }

    private String getSessionToken() {
        try {
            String path = System.getProperty("user.dir") + "/src/main/java/poker/connection/client/";
            File file = new File(path + "session.txt");
            Scanner scanner = new Scanner(file);
            return scanner.nextLine();
        } catch (IOException e) {
            return null;
        }
    }

    private void saveSessionToken(String token) {
        try {
            String path = System.getProperty("user.dir") + "/src/main/java/poker/connection/client/";
            File file = new File(path + "session.txt");
            FileWriter writer = new FileWriter(file);
            writer.write(token);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
