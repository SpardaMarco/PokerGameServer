package connection.client;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Scanner;

public class Client {

    final static String INPUT_REQ = "INPUT_REQUEST";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        SSLSocket socket = connect(host, port);
        authenticate(socket);
    }

    private static SSLSocket connect(String host, int port) throws Exception {

        SSLContext sslContext = getSSLContext();

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        SSLSocket socket = (SSLSocket) socketFactory.createSocket();

        SocketAddress socketAddress = new InetSocketAddress(host, port);
        socket.connect(socketAddress);
        socket.startHandshake();

        return socket;
    }

    private static SSLContext getSSLContext() throws Exception {
        FileInputStream trustStoreInputStream = new FileInputStream("connection/client/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(trustStoreInputStream, "client_keystore".toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    private static void authenticate(SSLSocket socket) throws Exception {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        handleServerIO(reader, writer);
    }

    private static void handleServerIO(BufferedReader reader, PrintWriter writer) throws Exception {

        while(true) {
            String streamOutput = reader.readLine();
            if (streamOutput != null) {
                if (streamOutput.equals(INPUT_REQ)) {
                    String userInput = new Scanner(System.in).nextLine();
                    writer.println(userInput);
                } else {
                    System.out.println(streamOutput);
                }
            }
        }
    }
}
