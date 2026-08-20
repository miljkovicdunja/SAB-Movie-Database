package student;

import rs.ac.bg.etf.sab.operations.TagsOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class md210500_TagsOperations implements TagsOperations {

    private Integer findTagId(Connection conn, String tagName) {
        try (PreparedStatement find = conn.prepareStatement("SELECT ID FROM Tags WHERE Name = ?");) {
            find.setString(1, tagName);
            ResultSet rs = find.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
        } return null;
    }
    @Override
    public Integer addTag(Integer id, String tag) {
        Connection conn = DB.getInstance().getConnection();
        try {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Tags (Name) VALUES (?)")) {
                ps.setString(1, tag);
                ps.executeUpdate();
            } catch (SQLException e) {
                //continue
            }

            Integer tagId = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT ID FROM Tags WHERE Name = ?")) {
                ps.setString(1, tag);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tagId = rs.getInt(1);
                    }
                }
            }

            String sqlLink = "INSERT INTO MovieTags (MovieID, TagID) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlLink)) {
                ps.setInt(1, id);
                ps.setInt(2, tagId);
                ps.executeUpdate();
            }
            return id;

        } catch (SQLException e) {return null;}


    }

    @Override
    public Integer removeTag(Integer movie, String tag) {
        Connection conn = DB.getInstance().getConnection();
        Integer tagId = findTagId(conn, tag);
        if (tagId == null) return null;
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MovieTags WHERE MovieID = ? AND TagID = ?");){
            stmt.setInt(1, movie);
            stmt.setInt(2, tagId);
            int rows = stmt.executeUpdate();
            return rows > 0 ? movie : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove tag", e);
        }
    }

    @Override
    public int removeAllTagsForMovie(Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MovieTags WHERE MovieID = ?");){
            stmt.setInt(1, movie);
            return stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove all tags for movie", e);
        }
    }

    @Override
    public boolean hasTag(Integer movie, String tag) {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM MovieTags mt JOIN Tags t ON t.ID = mt.TagID " + "WHERE mt.MovieID = ? AND t.Name = ?");){
            stmt.setInt(1, movie);
            stmt.setString(2, tag);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            throw new RuntimeException("Failed to check if movie has tag", e);
        }
    }

    @Override
    public List<String> getTagsForMovie(Integer movie) {
        Connection conn = DB.getInstance().getConnection();
        List<String> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT t.Name FROM MovieTags mt JOIN Tags t ON t.ID = mt.TagID " + "WHERE mt.MovieID = ?");){
            stmt.setInt(1, movie);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get tags for movie", e);
        }
    }

    @Override
    public List<Integer> getMovieIdsByTag(String tag) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT mt.MovieID FROM MovieTags mt JOIN Tags t ON t.ID = mt.TagID " + "WHERE t.Name = ?");){
            stmt.setString(1, tag);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get movie ids by tag", e);
        }
    }

    @Override
    public List<String> getAllTags() {
        Connection conn = DB.getInstance().getConnection();
        List<String> result = new ArrayList<>();

        try ( Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT DISTINCT t.Name FROM MovieTags mt JOIN Tags t ON t.ID = mt.TagID");){
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all tags", e);
        }
    }
}
