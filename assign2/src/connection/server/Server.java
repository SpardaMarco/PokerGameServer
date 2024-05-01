package connection.server;

import connection.protocol.Channel;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.util.*;

public class Server {
    private Queue<String> playersQueue = new LinkedList<>();
    private Dictionary<String, Channel> connections = new Hashtable<>();
    private int port;
    private boolean loggingEnabled;
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java TimeServer <port> [-l]");
            return;
        }

        int port = Integer.parseInt(args[0]);

        if (args.length == 2 && args[1].equals("-l")) {
            new Server(port, true).init();
        } else if (args.length == 2) {
            System.out.println("Usage: java TimeServer <port> [-l]");
        } else {
            new Server(port, false).init();
        }
    }

    private Server(int port, boolean loggingEnabled) {
        this.port = port;
        this.loggingEnabled = loggingEnabled;
    }

    private void init() {
        SSLServerSocketFactory serverSocketFactory = getServerSocketFactory();
        try (SSLServerSocket serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                new Authenticator(new Channel(socket), this).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SSLServerSocketFactory getServerSocketFactory() {
        SSLServerSocketFactory serverSocketFactory;
        try {
            FileInputStream keyStoreInputStream = new FileInputStream("connection/server/server_keystore.p12");
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(keyStoreInputStream, "server_keystore".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "server_keystore".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            serverSocketFactory = sslContext.getServerSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return serverSocketFactory;
    }

    public synchronized void queuePlayer(String player, Channel socket) {

        playersQueue.add(player);
        connections.put(player, socket);

        if (this.loggingEnabled) {
            System.out.println("Player " + player + " has joined the game");
            System.out.println("Players in queue: " + playersQueue.size());
        }
    }
}
