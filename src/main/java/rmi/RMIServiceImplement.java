package rmi;

import dao.BookDAO;
import model.Book;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;

public class RMIServiceImplement extends UnicastRemoteObject implements RMIServiceInterface {
    private final BookDAO bookDAO;

    public RMIServiceImplement() throws RemoteException, SQLException {
        super();
        this.bookDAO = BookDAO.getInstance();
    }

    @Override
    public List<Book> searchBooks(String query) throws RemoteException {
        try {
            return bookDAO.searchBooks(query);
        } catch (SQLException e) {
            throw new RemoteException("Database error during search", e);
        }
    }

    @Override
    public boolean purchaseBook(String isbn, int quantity) throws RemoteException {
        try {
            Book book = bookDAO.readBook(isbn);
            if (book == null)
                throw new RemoteException("Book not found");
            if (book.getQuantity() < quantity)
                return false;

            book.setQuantity(book.getQuantity() - quantity);
            bookDAO.updateBook(book);
            return true;
        } catch (SQLException e) {
            throw new RemoteException("Purchase failed", e);
        }
    }

    @Override
    public Book getBookDetails(String isbn) throws RemoteException {
        try {
            return bookDAO.readBook(isbn);
        } catch (SQLException e) {
            throw new RemoteException("Error retrieving book details", e);
        }
    }

    @Override
    public List<Book> listAvailableBooks() throws RemoteException {
        try {
            return bookDAO.searchBooks(""); // Returns all books
        } catch (SQLException e) {
            throw new RemoteException("Error listing books", e);
        }
    }

    @Override
    public int checkAvailability(String isbn) throws RemoteException {
        try {
            Book book = bookDAO.readBook(isbn);
            return (book != null) ? book.getQuantity() : -1;
        } catch (SQLException e) {
            throw new RemoteException("Error checking availability", e);
        }
    }
}