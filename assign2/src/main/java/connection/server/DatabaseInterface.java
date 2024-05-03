package connection.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseInterface {
    private Connection database;
    public DatabaseInterface() throws SQLException {

        String dbFile =  ClassLoader.getSystemResource("poker.db").getFile();
        try  {
            database = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            if (database != null) {
//                URI sqlURI = ClassLoader.getSystemResource("poker.sql").toURI();
//                Path sqlPath = Paths.get(sqlURI);
//                String sql = new String(Files.readAllBytes(sqlPath), StandardCharsets.UTF_8);

                String sql = "DROP TABLE IF EXISTS users;\n" +
                        "CREATE TABLE IF NOT EXISTS users (\n" +
                        "    username TEXT PRIMARY KEY NOT NULL,\n" +
                        "    password TEXT NOT NULL,\n" +
                        "    rank INTEGER DEFAULT 0,\n" +
                        "    session_token TEXT UNIQUE DEFAULT NULL,\n" +
                        "    token_expiration_date DATETIME DEFAULT NULL\n" +
                        ");\n" +
                        "INSERT INTO users (username, password)\n" +
                        "VALUES ('user1', 'password1');\n" +
                        "INSERT INTO users (username, password)\n" +
                        "VALUES ('user2', 'password2');\n" +
                        "INSERT INTO users (username, password)\n" +
                        "VALUES ('admin', 'admin_password');";

                Statement stmt = database.createStatement();
                stmt.execute(sql);

                System.out.println("Database initialized successfully.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean authenticateUser(String username, String password) throws SQLException {

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement pstmt = database.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, password);

        return pstmt.executeQuery().next();
    }
}
