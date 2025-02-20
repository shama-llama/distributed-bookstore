package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Book;

public interface RMIServiceInterface extends Remote {
    List<Book> searchBooks(String query) throws RemoteException;
    boolean purchaseBook(String isbn, int quantity) throws RemoteException;
    Book getBookDetails(String isbn) throws RemoteException;
    List<Book> listAvailableBooks() throws RemoteException;
    int checkAvailability(String isbn) throws RemoteException;
}