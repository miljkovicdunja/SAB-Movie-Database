package student;

import rs.ac.bg.etf.sab.operations.RatingsOperations;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class md210500_RatingsOperations implements RatingsOperations {
    private void rewardCheck(Integer user, Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        try(CallableStatement cs = conn.prepareCall("{call SP_REWARD_USER_(?, ?)}");){
            cs.setInt(1, user);
            cs.setInt(2, movie);
            cs.execute();

        } catch (Exception e) {
            throw new RuntimeException("Failed to check reward", e);
        }
    }
    @Override
    public boolean addRating(Integer user, Integer movie, Integer score) {
        Connection conn = DB.getInstance().getConnection();
        if (getRating(user, movie) != null) return false;

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Ratings(UserId, MovieId, Score, CreatedAt, RatingDate) VALUES (?, ?, ?, GETDATE(), GETDATE())");){
            stmt.setInt(1, user);
            stmt.setInt(2, movie);
            stmt.setInt(3, score);
            stmt.executeUpdate();

            rewardCheck(user, movie);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateRating(Integer user, Integer movie, Integer score) {
        Connection conn = DB.getInstance().getConnection();
        if (getRating(user, movie) == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE Ratings SET Score = ?, RatingDate = GETDATE() WHERE UserID = ? AND MovieID = ?");){
            stmt.setInt(1, score);
            stmt.setInt(2, user);
            stmt.setInt(3, movie);
            stmt.executeUpdate();

            rewardCheck(user, movie);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeRating(Integer user, Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Ratings WHERE UserID = ? AND MovieID = ?");){
            stmt.setInt(1, user);
            stmt.setInt(2, movie);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove rating", e);
        }
    }

    @Override
    public Integer getRating(Integer user, Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Score FROM Ratings WHERE UserID = ? AND MovieID = ?");){
            stmt.setInt(1, user);
            stmt.setInt(2, movie);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get rating", e);
        }
    }

    @Override
    public List<Integer> getRatedMoviesByUser(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT MovieID FROM Ratings WHERE UserID = ?");){
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get rated movies by user", e);
        }
    }

    @Override
    public List<Integer> getUsersWhoRatedMovie(Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT UserID FROM Ratings WHERE MovieID = ?");){
            stmt.setInt(1, movie);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get users who rated movie", e);
        }
    }
}
