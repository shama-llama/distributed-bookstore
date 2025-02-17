// BookstoreRPCServiceImpl.java (RPC Implementation)
package rpc;

import dao.BookDAO;
import model.Book;
import jakarta.jws.WebService;
import java.sql.SQLException;
import java.util.List;

@WebService(endpointInterface = "rpc.RPCServiceInterface")
public class RPCServiceImplement implements RPCServiceInterface {
    private final BookDAO bookDAO;

    public RPCServiceImplement() throws SQLException {
        this.bookDAO = BookDAO.getInstance();
    }

    @Override
    public boolean createBook(Book book) {
        try {
            bookDAO.createBook(book);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Book readBook(String isbn) {
        try {
            return bookDAO.readBook(isbn);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateBook(Book book) {
        try {
            bookDAO.updateBook(book);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteBook(String isbn) {
        try {
            bookDAO.deleteBook(isbn);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Book> searchBooks(String query) {
        try {
            return bookDAO.searchBooks(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public boolean bulkUploadBooks(List<Book> books) {
        try {
            bookDAO.bulkUploadBooks(books);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}