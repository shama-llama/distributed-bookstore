package rmiserver;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class BookStoreServer {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            BookStoreImplement bookStore = new BookStoreImplement();
            Naming.rebind("rmi://localhost:1099/BookStoreService", bookStore);
            System.out.println("BookStore Server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}