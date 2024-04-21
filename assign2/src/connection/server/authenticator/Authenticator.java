package connection.server.authenticator;

import connection.server.Server;

import javax.net.ssl.SSLSocket;
import java.io.*;

public class Authenticator extends Thread {

    private SSLSocket socket;

    public Authenticator(SSLSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            writer.println("Welcome to Poker!");
            writer.println("Please enter your username:");

            String username = reader.readLine();
            writer.println("Please enter your password:");

            String password = reader.readLine();

            if (username.equals("admin") && password.equals("admin")) {
                writer.println("Welcome, admin!");
                Server.queuePlayer("admin", socket);
            } else {
                writer.println("Invalid username or password. Please try again.");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
