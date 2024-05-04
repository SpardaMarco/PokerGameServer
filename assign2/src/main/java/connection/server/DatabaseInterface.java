package connection.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseInterface {

    private final Connection database;

    public DatabaseInterface() {

        String path = System.getProperty("user.dir") + "/src/database/";
        String dbFile =  path + "poker.db";
        try  {
            database = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            if (database != null) {
                System.out.println("Database connected successfully.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean authenticateUser(String username, String password) throws SQLException {

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement stmt = database.prepareStatement(query);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();

        return rs.next();
    }
}
