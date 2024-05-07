package poker.connection.server.database;

import java.io.IOException;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void main(String[] args) throws SQLException, IOException {


        DatabaseInterface database = new DatabaseInterface();
        database.reset();
        populate(database);
    }

    private static void populate(DatabaseInterface database) {

        database.registerUser("user1", "password1");
        database.registerUser("user2", "password2");
        database.registerUser("user3", "password3");
    }
}
