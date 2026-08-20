package student;

import rs.ac.bg.etf.sab.operations.MoviesOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class md210500_MoviesOperations implements MoviesOperations {

    @Override
    public Integer addMovie(String title, Integer genId, String director) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement checkGenre = conn.prepareStatement("SELECT 1 FROM Genres WHERE ID = ?");){
            checkGenre.setInt(1, genId);
            ResultSet rsGenre = checkGenre.executeQuery();
            if (!rsGenre.next()) return null;

            PreparedStatement insertMovie = conn.prepareStatement(
                    "INSERT INTO Movies(Title, Director) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertMovie.setString(1, title);
            insertMovie.setString(2, director);
            insertMovie.executeUpdate();

            ResultSet rs = insertMovie.getGeneratedKeys();
            if (!rs.next()) return null;
            int movieId = rs.getInt(1);

            PreparedStatement insertGenre = conn.prepareStatement(
                    "INSERT INTO MovieGenres(MovieID, GenreID) VALUES (?, ?)");
            insertGenre.setInt(1, movieId);
            insertGenre.setInt(2, genId);
            insertGenre.executeUpdate();

            return movieId;

        } catch (Exception e) {
            throw new RuntimeException("Failed to add movie", e);
        }
    }

    @Override
    public Integer updateMovieTitle(Integer id, String title) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE Movies SET Title = ? WHERE ID = ?");){
            stmt.setString(1, title);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? id : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to update movie title", e);
        }

    }

    @Override
    public Integer addGenreToMovie(Integer movieId, Integer genId) {
        Connection conn = DB.getInstance().getConnection();
        try {
            PreparedStatement checkMovie = conn.prepareStatement("SELECT 1 FROM Movies WHERE ID = ?");
            checkMovie.setInt(1, movieId);
            if (!checkMovie.executeQuery().next()) return null;

            PreparedStatement checkGenre = conn.prepareStatement("SELECT 1 FROM Genres WHERE ID = ?");
            checkGenre.setInt(1, genId);
            if (!checkGenre.executeQuery().next()) return null;

            PreparedStatement checkLink = conn.prepareStatement(
                    "SELECT 1 FROM MovieGenres WHERE MovieID = ? AND GenreID = ?");
            checkLink.setInt(1, movieId);
            checkLink.setInt(2, genId);
            if (checkLink.executeQuery().next()) return null;

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO MovieGenres(MovieID, GenreID) VALUES (?, ?)");
            insert.setInt(1, movieId);
            insert.setInt(2, genId);
            insert.executeUpdate();

            return movieId;

        } catch (Exception e) {
            throw new RuntimeException("Failed to add genre to movie", e);
        }

    }

    @Override
    public Integer removeGenreFromMovie(Integer movieId, Integer genId) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MovieGenres WHERE MovieID = ? AND GenreID = ?");){
            stmt.setInt(1, movieId);
            stmt.setInt(2, genId);
            int rows = stmt.executeUpdate();
            return rows > 0 ? movieId : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove genre from movie", e);
        }
    }

    @Override
    public Integer updateMovieDirector(Integer id, String director) {
        Connection conn = DB.getInstance().getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE Movies SET Director = ? WHERE ID = ?");
            stmt.setString(1, director);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? id : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to update movie director", e);
        }
    }

    @Override
    public Integer removeMovie(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        try {
            PreparedStatement deleteMovie = conn.prepareStatement("DELETE FROM Movies WHERE ID = ?");
            deleteMovie.setInt(1, id);
            int rows = deleteMovie.executeUpdate();
            return rows > 0 ? id : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove movie", e);
        }
    }

    @Override
    public List<Integer> getMovieIds(String title, String director) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT ID FROM Movies WHERE Title = ? AND Director = ?");
            stmt.setString(1, title);
            stmt.setString(2, director);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get movie ids", e);
        }
    }

    @Override
    public List<Integer> getAllMovieIds() {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ID FROM Movies");){
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all movie ids", e);
        }
    }

    @Override
    public List<Integer> getMovieIdsByGenre(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT MovieID FROM MovieGenres WHERE GenreID = ?");){
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get movie ids by genre", e);
        }
    }

    @Override
    public List<Integer> getGenreIdsForMovie(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT GenreId FROM MovieGenres WHERE MovieId = ?");){
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get genre ids for movie", e);
        }
    }

    @Override
    public List<Integer> getMovieIdsByDirector(String name) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT ID FROM Movies WHERE Director = ?");){
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get movie ids by director", e);
        }
    }

    @Override
    public String getMovieTrend(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT TrendStatus FROM Movies WHERE ID = ?");){
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get movie trend", e);
        }
    }
}
