package connection.server;

import java.sql.*;

public class DatabaseInterface {
    private final Connection database;
    public DatabaseInterface() {

        String dbFile =  ClassLoader.getSystemResource("poker.db").getFile();
        try  {
            database = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            if (database != null) {
//                URI sqlURI = ClassLoader.getSystemResource("poker.sql").toURI();
//                Path sqlPath = Paths.get(sqlURI);
//                String sql = new String(Files.readAllBytes(sqlPath), StandardCharsets.UTF_8);

                String sql = """
                        DROP TABLE IF EXISTS users;
                        CREATE TABLE IF NOT EXISTS users (
                            username TEXT PRIMARY KEY NOT NULL,
                            password TEXT NOT NULL,
                            rank INTEGER DEFAULT 0,
                            session_token TEXT UNIQUE DEFAULT NULL,
                            token_expiration_date DATETIME DEFAULT NULL
                        );
                        INSERT INTO users (username, password)
                        VALUES ('user1', 'password1');
                        INSERT INTO users (username, password)
                        VALUES ('user2', 'password2');
                        INSERT INTO users (username, password)
                        VALUES ('admin', 'admin_password');""";

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
        PreparedStatement stmt = database.prepareStatement(query);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();

        return rs.next();
    }
}
