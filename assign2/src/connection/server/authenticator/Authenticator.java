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

        try {
            socket.startHandshake();
            authenticate();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void authenticate(){
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            writer.println("Welcome to Poker!");
            writer.println("Please enter your username:");
            writer.println("INPUT_REQUEST");

            String username = reader.readLine();
            writer.println("Please enter your password:");
            writer.println("INPUT_REQUEST");

            String password = reader.readLine();

            System.out.println(String.format(
                    "Received\nusername: %s\npassword: %s",
                    username,
                    password
            ));

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
