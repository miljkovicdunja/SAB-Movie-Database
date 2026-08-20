package student;

import rs.ac.bg.etf.sab.operations.UsersOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class md210500_UsersOperations implements UsersOperations {
    @Override
    public Integer addUser(String name) {
        if (doesUserExist(name)) return null;
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Users(Username, Rewards) VALUES (?, 0)", Statement.RETURN_GENERATED_KEYS);){
            stmt.setString(1, name);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Integer updateUser(Integer id, String username) {
        Connection conn = DB.getInstance().getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE Users SET Username = ? WHERE ID = ?");
            stmt.setString(1, username);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? id : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public Integer removeUser(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Users WHERE ID = ?");){
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? id : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove user", e);
        }
    }

    @Override
    public boolean doesUserExist(String name) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM Users WHERE Username = ?");){
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            throw new RuntimeException("Failed to check if user exists", e);
        }
    }

    @Override
    public Integer getUserId(String name) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT ID FROM Users WHERE Username = ?");){
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get user id", e);
        }
    }

    @Override
    public List<Integer> getAllUserIds() {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID FROM Users");){
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all user ids", e);
        }
    }

    @Override
    public List<Integer> getRecommendedMoviesFromFavoriteGenres(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try {
            List<Integer> favoriteGenres = new ArrayList<>();
            PreparedStatement favStmt = conn.prepareStatement(
                    "SELECT mg.GenreID " +
                            "FROM Ratings r JOIN MovieGenres mg ON mg.MovieID = r.MovieID " +
                            "WHERE r.UserID = ? " +
                            "GROUP BY mg.GenreID " +
                            "HAVING AVG(CAST(r.Score AS DECIMAL(10,3))) >= 8");
            favStmt.setInt(1, id);
            ResultSet favRs = favStmt.executeQuery();
            while (favRs.next()) {
                favoriteGenres.add(favRs.getInt(1));
            }
            if (favoriteGenres.isEmpty()) return result;

            String placeholders = String.join(",", Collections.nCopies(favoriteGenres.size(), "?"));
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT m.Id, AVG(CAST(r.Score AS DECIMAL(10,3))) AS AvgScore, COUNT(r.Score) AS RatingCount " +
                            "FROM Movies m " +
                            "JOIN MovieGenres mg ON mg.MovieId = m.Id " +
                            "LEFT JOIN Ratings r ON r.MovieId = m.Id " +
                            "WHERE mg.GenreId IN (" + placeholders + ") " +
                            "  AND m.Id NOT IN (SELECT MovieId FROM Ratings WHERE UserId = ?) " +
                            "  AND m.Id NOT IN (SELECT MovieId FROM Watchlist WHERE UserId = ?) " +
                            "GROUP BY m.Id " +
                            "HAVING (COUNT(r.Score) >= 4 AND AVG(CAST(r.Score AS DECIMAL(10,3))) >= 7.5) " +
                            "    OR (COUNT(r.Score) < 4 AND AVG(CAST(r.Score AS DECIMAL(10,3))) >= 9) " +
                            "ORDER BY AvgScore DESC, m.Id ASC");

            int idx = 1;
            for (Integer genreId : favoriteGenres) {
                stmt.setInt(idx++, genreId);
            }
            stmt.setInt(idx++, id);
            stmt.setInt(idx++, id);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get recommended movies", e);
        }
    }

    @Override
    public Integer getRewards(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Rewards FROM Users WHERE Id = ?");){
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get rewards", e);
        }
    }

    @Override
    public List<String> getThematicSpecializations(Integer id) {
        String sql = "SELECT tg.Name " +
                "FROM Ratings r " +
                "JOIN MovieTags mt ON mt.MovieID = r.MovieID " +
                "JOIN Tags tg ON tg.ID = mt.TagID " +
                "WHERE r.UserID = ? AND r.Score >= 8 " +
                "GROUP BY tg.Name " +
                "HAVING COUNT(DISTINCT mt.MovieID) >= 2";;
        List<String> tags = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString(1));
                }
            }
            return tags;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get specializations", e);
        }

    }

    @Override
    public String getUserDescription(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Ratings WHERE UserID = ?");){
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int ratedCount = rs.getInt(1);

            if (ratedCount < 10) return "undefined";

            PreparedStatement tagStmt = conn.prepareStatement(
                    "SELECT COUNT(DISTINCT tg.Name) " +
                            "FROM Ratings r " +
                            "JOIN MovieTags mt ON mt.MovieID = r.MovieID " +
                            "JOIN Tags tg ON tg.ID = mt.TagID " +
                            "WHERE r.UserID = ?");
            tagStmt.setInt(1, id);
            ResultSet tagRs = tagStmt.executeQuery();
            tagRs.next();
            int distinctTags = tagRs.getInt(1);

            return distinctTags >= 10 ? "curious" : "focused";

        } catch (Exception e) {
            throw new RuntimeException("Failed to get user description", e);
        }
    }
}
