package connection.server.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

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

    public boolean userExists(String username) throws SQLException {

        String query = "SELECT * FROM users WHERE username = ?";

        PreparedStatement stmt = database.prepareStatement(query);
        stmt.setString(1, username);

        return stmt.executeQuery().next();
    }

    public boolean authenticateUser(String username, String password) throws SQLException {

        String query = "SELECT password FROM users WHERE username = ?";
        PreparedStatement stmt = database.prepareStatement(query);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            return false;
        }

        String hashedPassword = rs.getString("password");
        return BCrypt.checkpw(password, hashedPassword);
    }

    public boolean registerUser(String username, String password) {

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try {
            PreparedStatement stmt = database.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean createSession(String username, String sessionToken, long duration) {

        String query = "UPDATE users SET session_token = ?, session_expiration = ? WHERE username = ?";

        try {
            PreparedStatement stmt = database.prepareStatement(query);
            stmt.setString(1, sessionToken);
            stmt.setDate(2, new Date(System.currentTimeMillis() + duration));
            stmt.setString(3, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public String recoverSession(String sessionToken) {

        String query = "SELECT username FROM users WHERE session_token = ? AND session_expiration > ?";

        try {
            PreparedStatement stmt = database.prepareStatement(query);
            stmt.setString(1, sessionToken);
            stmt.setDate(2, new Date(System.currentTimeMillis()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateRank(String username, int rankIncrement) {

        String query = "UPDATE users SET rank = rank + ? WHERE username = ?";
        try {
            PreparedStatement stmt = database.prepareStatement(query);
            stmt.setInt(1, rankIncrement);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() throws IOException, SQLException {

        String sqlFile = System.getProperty("user.dir") + "/src/database/poker.sql";

        String sql = Files.readString(Paths.get(sqlFile));

        database.createStatement().executeUpdate(sql);
    }

    public void close() {
        try {
            database.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
