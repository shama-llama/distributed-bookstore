package dao;

import model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BookDAO {
    private static BookDAO instance;
    private Connection connection;

    private BookDAO() throws SQLException {
        try {
            Properties props = new Properties();
            props.load(getClass().getClassLoader().getResourceAsStream("db.properties"));
            
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");
            
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new SQLException("Error initializing database connection", e);
        }
    }

    public static synchronized BookDAO getInstance() throws SQLException {
        if (instance == null) {
            instance = new BookDAO();
        }
        return instance;
    }

    // CRUD Operations
    public void createBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, year, price, quantity) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setInt(4, book.getYear());
            stmt.setDouble(5, book.getPrice());
            stmt.setInt(6, book.getQuantity());
            stmt.executeUpdate();
        }
    }

    public Book readBook(String isbn) throws SQLException {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("year"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                );
            }
            return null;
        }
    }

    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title=?, author=?, year=?, price=?, quantity=? WHERE isbn=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setInt(3, book.getYear());
            stmt.setDouble(4, book.getPrice());
            stmt.setInt(5, book.getQuantity());
            stmt.setString(6, book.getIsbn());
            stmt.executeUpdate();
        }
    }

    public void deleteBook(String isbn) throws SQLException {
        String sql = "DELETE FROM books WHERE isbn = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            stmt.executeUpdate();
        }
    }

    // Search and Inventory Operations
    public List<Book> searchBooks(String query) throws SQLException {
        List<Book> results = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("year"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                ));
            }
        }
        return results;
    }

    public void bulkUploadBooks(List<Book> books) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, year, price, quantity) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Book book : books) {
                stmt.setString(1, book.getIsbn());
                stmt.setString(2, book.getTitle());
                stmt.setString(3, book.getAuthor());
                stmt.setInt(4, book.getYear());
                stmt.setDouble(5, book.getPrice());
                stmt.setInt(6, book.getQuantity());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}