package connection.client;

import connection.protocol.Channel;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.util.Scanner;

import static connection.protocol.Flag.*;

public class Client {
    private final Channel channel;
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
        channel = connect(host, port);
    }

    private void init() {
        String session = getSession();
        if (session != null) {
            channel.sendRecoverSession(session);
        } else {
            channel.sendNewConnection();
        }
        handleServerIO();
    }

    private Channel connect(String host, int port) throws Exception {

        SSLContext sslContext = getSSLContext();

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        SSLSocket socket = (SSLSocket) socketFactory.createSocket();

        SocketAddress socketAddress = new InetSocketAddress(host, port);
        socket.connect(socketAddress);
        socket.startHandshake();

        return new Channel(socket);
    }

    private SSLContext getSSLContext() throws Exception {

        InputStream trustStoreInputStream = getClass().getClassLoader().getResourceAsStream("client_keystore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(trustStoreInputStream, "client_keystore".toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    private void handleServerIO() {

        String incoming;
        while((incoming = channel.getResponse()) != null) {

            if (INPUT_REQ.equals(incoming)) {
                String userInput = new Scanner(System.in).nextLine();
                channel.sendMessage(userInput);
            } else if (END_CONNECTION.equals(incoming)) {
                System.out.println("Server ended the connection.");
            } else if (NEW_SESSION.equals(incoming)) {
                String token = channel.getResponse();
                saveSession(token);
            }
            else {
                System.out.println(incoming);
            }
        }
    }

    private String getSession() {
        try {
            String path = System.getProperty("user.dir") + "/src/main/java/connection/client/";
            File file = new File(path + "session.txt");
            Scanner scanner = new Scanner(file);
            return scanner.nextLine();
        } catch (IOException e) {
            return null;
        }
    }

    private void saveSession(String token) {
        try {
            String path = System.getProperty("user.dir") + "/src/main/java/connection/client/";
            File file = new File(path + "session.txt");
            FileWriter writer = new FileWriter(file);
            writer.write(token);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
