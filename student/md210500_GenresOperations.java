package student;

import rs.ac.bg.etf.sab.operations.GenresOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class md210500_GenresOperations implements GenresOperations {
    @Override
    public Integer addGenre(String name) {
        Connection conn = DB.getInstance().getConnection();
        if (doesGenreExist(name)) return null;
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Genres(Name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);){
            stmt.setString(1, name);
            stmt.executeUpdate();

            try(ResultSet rs = stmt.getGeneratedKeys();) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to add genre", e);
        }
    }

    @Override
    public Integer updateGenre(Integer id, String newName) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE Genres SET Name = ? WHERE ID = ?");){
            stmt.setString(1, newName);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? id : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to update genre", e);
        }
    }

    @Override
    public Integer removeGenre(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Genres WHERE ID = ?");){
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? id : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove genre", e);
        }
    }

    @Override
    public boolean doesGenreExist(String name) {
        Connection conn = DB.getInstance().getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT count(*) FROM Genres WHERE Name = ?");
            stmt.setString(1, name);
            try(ResultSet rs = stmt.executeQuery();){
                if(rs.next())return rs.getInt(1)>0;
                return false;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to check if genre exists", e);
        }
    }

    @Override
    public Integer getGenreId(String name) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT ID FROM Genres WHERE Name = ?");){
            stmt.setString(1, name);
            try(ResultSet rs = stmt.executeQuery();) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get genre id", e);
        }
    }

    @Override
    public List<Integer> getAllGenreIds() {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ID FROM Genres");
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all genre ids", e);
        }
    }
}
