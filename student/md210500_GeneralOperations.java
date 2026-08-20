package student;

import rs.ac.bg.etf.sab.operations.GeneralOperations;

import java.sql.Connection;
import java.sql.Statement;

public class md210500_GeneralOperations implements GeneralOperations {

    @Override
    public void eraseAll() {
        Connection conn = DB.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM UserSpecializations");
            stmt.executeUpdate("DELETE FROM Ratings");
            stmt.executeUpdate("DELETE FROM Watchlist");
            stmt.executeUpdate("DELETE FROM MovieTags");
            stmt.executeUpdate("DELETE FROM MovieGenres");
            stmt.executeUpdate("DELETE FROM Movies");
            stmt.executeUpdate("DELETE FROM Tags");
            stmt.executeUpdate("DELETE FROM Genres");
            stmt.executeUpdate("DELETE FROM Users");

        } catch (Exception e) {
            throw new RuntimeException("Failed to erase all data", e);
        }
    }
}
