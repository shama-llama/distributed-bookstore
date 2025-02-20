package rpc;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.util.List;
import model.Book;

@WebService
public interface RPCServiceInterface {
    @WebMethod
    boolean createBook(Book book);

    @WebMethod
    Book readBook(String isbn);

    @WebMethod
    boolean updateBook(Book book);

    @WebMethod
    boolean deleteBook(String isbn);

    @WebMethod
    List<Book> searchBooks(String query);

    @WebMethod
    boolean bulkUploadBooks(List<Book> books);
}