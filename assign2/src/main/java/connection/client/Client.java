package connection.client;

import connection.protocol.Channel;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.util.Scanner;

import static connection.protocol.Flag.CONNECTION_END;
import static connection.protocol.Flag.INPUT_REQ;

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
            } else if (CONNECTION_END.equals(incoming)) {
                System.out.println("Server ended the connection.");
            } else {
                System.out.println(incoming);
            }
        }
    }
}
