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
        database.registerUserWithRank("marco", "marco", 1000);
        database.registerUserWithRank("tiago", "tiago", 2000);
        database.registerUserWithRank("ramos", "ramos", 1000);
        database.registerUserWithRank("joao", "joao", 1500);
        database.registerUserWithRank("rita", "rita", 500);
        database.registerUserWithRank("jorge", "jorge", -10000);
        database.registerUserWithRank("afonso", "afonso", 5000);
        database.registerUserWithRank("camilla", "camilla", 5000);
        database.registerUser("baquero", "baquero");
        database.registerUser("alberto", "alberto");
        database.registerUser("veronica", "veronica");
    }
}
