package student;

import rs.ac.bg.etf.sab.operations.WatchlistsOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class md210500_WatchlistsOperations implements WatchlistsOperations {
    @Override
    public boolean addMovieToWatchlist(Integer user, Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        if (isMovieInWatchlist(user, movie)) return false;
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Watchlist(UserID, MovieID) VALUES (?, ?)");){
            stmt.setInt(1, user);
            stmt.setInt(2, movie);
            stmt.executeUpdate();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeMovieFromWatchlist(Integer user, Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Watchlist WHERE UserID = ? AND MovieID = ?");){
            stmt.setInt(1, user);
            stmt.setInt(2, movie);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove movie from watchlist", e);
        }
    }

    @Override
    public boolean isMovieInWatchlist(Integer user, Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT 1 FROM Watchlist WHERE UserID = ? AND MovieID = ?");
            stmt.setInt(1, user);
            stmt.setInt(2, movie);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            throw new RuntimeException("Failed to check if movie is in watchlist", e);
        }
    }

    @Override
    public List<Integer> getMoviesInWatchlist(Integer user) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT MovieId FROM Watchlist WHERE UserID = ?");){
            stmt.setInt(1, user);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get movies in watchlist", e);
        }
    }

    @Override
    public List<Integer> getUsersWithMovieInWatchlist(Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT UserID FROM Watchlist WHERE MovieID = ?");){
            stmt.setInt(1, movie);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get users with movie in watchlist", e);
        }
    }
}
