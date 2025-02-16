package rmiserver;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookStoreImplement extends UnicastRemoteObject implements BookStoreInterface {
    private Connection connection;

    public BookStoreImplement() throws RemoteException {
        super();
        initializeDatabase();
    }

    private void initializeDatabase() throws RemoteException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bookstore", 
                "root", 
                "root_password"
            );
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS books ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "title VARCHAR(255) NOT NULL,"
                + "author VARCHAR(255) NOT NULL,"
                + "price DECIMAL(10,2) NOT NULL,"
                + "quantity INT NOT NULL)";
                
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
            }
        } catch (Exception e) {
            throw new RemoteException("Database initialization failed: " + e.getMessage());
        }
    }

    @Override
    public void addBook(Book book) throws RemoteException {
        String sql = "INSERT INTO books (title, author, price, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setDouble(3, book.getPrice());
            pstmt.setInt(4, book.getQuantity());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RemoteException("Error adding book: " + e.getMessage());
        }
    }

    @Override
    public void removeBook(int bookId) throws RemoteException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RemoteException("Error removing book: " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfAvailableBooks() throws RemoteException {
        String sql = "SELECT SUM(quantity) AS total FROM books";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            throw new RemoteException("Error getting total books: " + e.getMessage());
        }
    }

    @Override
    public List<Book> getAllBooks() throws RemoteException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            throw new RemoteException("Error retrieving books: " + e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> searchBooks(String query) throws RemoteException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                books.add(new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            throw new RemoteException("Error searching books: " + e.getMessage());
        }
        return books;
    }

    @Override
    public void purchaseBook(int bookId, int quantity) throws RemoteException {
        try {
            connection.setAutoCommit(false);
            
            // Check availability
            String checkSQL = "SELECT quantity FROM books WHERE id = ? FOR UPDATE";
            try (PreparedStatement pstmt = connection.prepareStatement(checkSQL)) {
                pstmt.setInt(1, bookId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int available = rs.getInt("quantity");
                    if (available < quantity) {
                        throw new RemoteException("Insufficient stock");
                    }
                    
                    // Update stock
                    String updateSQL = "UPDATE books SET quantity = quantity - ? WHERE id = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSQL)) {
                        updateStmt.setInt(1, quantity);
                        updateStmt.setInt(2, bookId);
                        updateStmt.executeUpdate();
                        connection.commit();
                    }
                } else {
                    throw new RemoteException("Book not found");
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RemoteException("Transaction failed: " + ex.getMessage());
            }
            throw new RemoteException("Purchase failed: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RemoteException("AutoCommit reset failed: " + e.getMessage());
            }
        }
    }
}