package connection.server;

import connection.server.authenticator.Authenticator;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class Server {

    private static Queue<String> playersQueue = new LinkedList<>();

    private static Dictionary<String, SSLSocket> connections = new Hashtable<>();

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java TimeServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

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
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (SSLServerSocket serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {

                SSLSocket socket = (SSLSocket) serverSocket.accept();

                new Authenticator(socket).run();

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void queuePlayer(String player, SSLSocket socket) {

       playersQueue.add(player);
       connections.put(player, socket);
    }
}
