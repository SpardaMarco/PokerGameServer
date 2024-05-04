package connection.server.database;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void main(String[] args) {

        String path = System.getProperty("user.dir") + "/src/database/";
        String dbFile =  path + "poker.db";
        try (Connection database = DriverManager.getConnection("jdbc:sqlite:" + dbFile)) {
            if (database != null) {
                String sqlFile = path + "poker.sql";
                String sql = Files.readString(Paths.get(sqlFile));
                database.createStatement().executeUpdate(sql);

                System.out.println("Database initialized successfully.");
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
