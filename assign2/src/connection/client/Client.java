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

public class Client {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        SSLSocketFactory socketFactory = null;
        try {

            FileInputStream trustStoreInputStream = new FileInputStream("connection/client/truststore.jks");
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(trustStoreInputStream, "client_keystore".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            socketFactory = sslContext.getSocketFactory();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }

        SocketAddress socketAddress = new InetSocketAddress(host, port);

        try (SSLSocket socket = (SSLSocket) socketFactory.createSocket()) {

            socket.connect(socketAddress);

            socket.addHandshakeCompletedListener(event -> {
                System.out.println("Handshake finished!");
            });

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            reader.lines().forEach(System.out::println);

            writer.println("admin");

            reader.lines().forEach(System.out::println);

            writer.println("admin");

            reader.lines().forEach(System.out::println);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
