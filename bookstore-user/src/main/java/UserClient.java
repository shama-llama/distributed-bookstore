import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import model.Book;
import rmi.RMIServiceInterface;

public class UserClient extends JFrame {
    private RMIServiceInterface rmiService;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JTextField isbnField;
    private JTextField quantityField;

    public UserClient() {
        initializeRMI();
        setupUI();
    }

    private void initializeRMI() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            rmiService = (RMIServiceInterface) registry.lookup("BookstoreService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to server: " + e.getMessage(), 
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void setupUI() {
        setTitle("Bookstore Client");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Search Panel
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(30);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchBooks());
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Book Table
        String[] columns = {"ISBN", "Title", "Author", "Year", "Price", "Quantity"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getSelectionModel().addListSelectionListener(e -> showBookDetails());

        // Details Panel
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2));
        JLabel isbnLabel = new JLabel("ISBN:");
        JLabel titleLabel = new JLabel("Title:");
        JLabel authorLabel = new JLabel("Author:");
        JLabel yearLabel = new JLabel("Year:");
        JLabel priceLabel = new JLabel("Price:");
        JLabel quantityLabel = new JLabel("Quantity:");
        
        JTextField detailIsbn = new JTextField();
        JTextField detailTitle = new JTextField();
        JTextField detailAuthor = new JTextField();
        JTextField detailYear = new JTextField();
        JTextField detailPrice = new JTextField();
        JTextField detailQuantity = new JTextField();
        
        detailIsbn.setEditable(false);
        detailTitle.setEditable(false);
        detailAuthor.setEditable(false);
        detailYear.setEditable(false);
        detailPrice.setEditable(false);
        detailQuantity.setEditable(false);

        detailsPanel.add(isbnLabel);
        detailsPanel.add(detailIsbn);
        detailsPanel.add(titleLabel);
        detailsPanel.add(detailTitle);
        detailsPanel.add(authorLabel);
        detailsPanel.add(detailAuthor);
        detailsPanel.add(yearLabel);
        detailsPanel.add(detailYear);
        detailsPanel.add(priceLabel);
        detailsPanel.add(detailPrice);
        detailsPanel.add(quantityLabel);
        detailsPanel.add(detailQuantity);

        // Purchase Panel
        JPanel purchasePanel = new JPanel();
        isbnField = new JTextField(15);
        quantityField = new JTextField(5);
        JButton purchaseButton = new JButton("Purchase");
        purchaseButton.addActionListener(e -> purchaseBook());

        purchasePanel.add(new JLabel("ISBN:"));
        purchasePanel.add(isbnField);
        purchasePanel.add(new JLabel("Quantity:"));
        purchasePanel.add(quantityField);
        purchasePanel.add(purchaseButton);

        // Main Layout
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(detailsPanel, BorderLayout.NORTH);
        rightPanel.add(purchasePanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);
    }

    private void searchBooks() {
        try {
            List<Book> books = rmiService.searchBooks(searchField.getText().trim());
            tableModel.setRowCount(0);
            
            for (Book book : books) {
                tableModel.addRow(new Object[]{
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYear(),
                    String.format("$%.2f", book.getPrice()),
                    book.getQuantity()
                });
            }
        } catch (Exception e) {
            showError("Search Error", e);
        }
    }

    private void showBookDetails() {
        int row = ((JTable)getContentPane().getComponent(1)).getSelectedRow();
        if (row >= 0) {
            String isbn = (String) tableModel.getValueAt(row, 0);
            try {
                Book book = rmiService.getBookDetails(isbn);
                if (book != null) {
                    updateDetailFields(book);
                    isbnField.setText(book.getIsbn());
                }
            } catch (Exception e) {
                showError("Details Error", e);
            }
        }
    }

    private void updateDetailFields(Book book) {
        Component[] components = ((JPanel)getContentPane().getComponent(2)).getComponents();
        ((JTextField) components[1]).setText(book.getIsbn());
        ((JTextField) components[3]).setText(book.getTitle());
        ((JTextField) components[5]).setText(book.getAuthor());
        ((JTextField) components[7]).setText(String.valueOf(book.getYear()));
        ((JTextField) components[9]).setText(String.format("$%.2f", book.getPrice()));
        ((JTextField) components[11]).setText(String.valueOf(book.getQuantity()));
    }

    private void purchaseBook() {
        String isbn = isbnField.getText().trim();
        String quantityText = quantityField.getText().trim();
        
        if (isbn.isEmpty() || quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter ISBN and quantity", 
                "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            boolean success = rmiService.purchaseBook(isbn, quantity);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Purchase successful!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                searchBooks(); // Refresh results
            } else {
                JOptionPane.showMessageDialog(this, "Purchase failed - insufficient stock", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity format", 
                "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            showError("Purchase Error", e);
        }
    }

    private void showError(String title, Exception e) {
        JOptionPane.showMessageDialog(this, 
            "Error: " + e.getMessage(), 
            title, JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserClient client = new UserClient();
            client.setVisible(true);
        });
    }
}