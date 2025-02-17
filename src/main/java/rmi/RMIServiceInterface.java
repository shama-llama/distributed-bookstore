package rmiserver;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BookStoreInterface extends Remote {
    void addBook(Book book) throws RemoteException;
    void removeBook(int bookId) throws RemoteException;
    int getNumberOfAvailableBooks() throws RemoteException;
    List<Book> getAllBooks() throws RemoteException;
    List<Book> searchBooks(String query) throws RemoteException;
    void purchaseBook(int bookId, int quantity) throws RemoteException;
}