package connection.server.database;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void main(String args[]) {

        String dbFile =  ClassLoader.getSystemResource("poker.db").getFile();
        try (Connection database = DriverManager.getConnection("jdbc:sqlite:" + dbFile)) {
            if (database != null) {
                URI sqlURI = ClassLoader.getSystemResource("poker.sql").toURI();
                Path sqlPath = Paths.get(sqlURI);
                String sql = new String(Files.readAllBytes(sqlPath), StandardCharsets.UTF_8);
                Statement stmt = database.createStatement();
                stmt.execute(sql);
                stmt.close();

                System.out.println("Database initialized successfully.");
            }
        } catch (SQLException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
