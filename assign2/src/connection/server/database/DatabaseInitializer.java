package connection.server.database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void main(String args[]) {

        String path = System.getProperty("user.dir") + "\\connection\\server\\database";
        String url = String.format("jdbc:sqlite:%s\\poker.db", path);
        String sqlFile = String.format("%s/poker.sql", path);
        try (Connection database = DriverManager.getConnection(url)) {
            if (database != null) {
                String sql = new String(Files.readAllBytes(Paths.get(sqlFile)), StandardCharsets.UTF_8);
                Statement stmt = database.createStatement();
                stmt.execute(sql);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
