import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import service.Book;
import service.RPCServiceImplementService;
import service.RPCServiceInterface;

public class AdminClient extends JFrame {
    private final RPCServiceInterface service;
    private JTable booksTable;
    private DefaultTableModel tableModel;

    public AdminClient() {
        // Initialize RPC service
        RPCServiceImplementService serviceImplement = new RPCServiceImplementService();
        service = serviceImplement.getRPCServiceImplementPort();

        // Configure main window
        setTitle("Bookstore Admin Panel");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create tabbed interface
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Create Book", createBookPanel());
        tabbedPane.addTab("Search/Manage", searchManagePanel());
        tabbedPane.addTab("Bulk Upload", bulkUploadPanel());

        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Form fields
        JTextField isbnField = new JTextField(20);
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField yearField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JTextField quantityField = new JTextField(20);

        // Add components
        addFormRow(panel, gbc, "ISBN:", isbnField, 0);
        addFormRow(panel, gbc, "Title:", titleField, 1);
        addFormRow(panel, gbc, "Author:", authorField, 2);
        addFormRow(panel, gbc, "Year:", yearField, 3);
        addFormRow(panel, gbc, "Price:", priceField, 4);
        addFormRow(panel, gbc, "Quantity:", quantityField, 5);

        // Submit button
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JButton submitButton = new JButton("Create Book");
        submitButton.addActionListener(e -> {
            try {
                Book book = new Book();
                book.setIsbn(isbnField.getText());
                book.setTitle(titleField.getText());
                book.setAuthor(authorField.getText());
                book.setYear(Integer.parseInt(yearField.getText()));
                book.setPrice(Double.parseDouble(priceField.getText()));
                book.setQuantity(Integer.parseInt(quantityField.getText()));

                boolean success = service.createBook(book);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Book created successfully!");
                    clearFields(isbnField, titleField, authorField, yearField, priceField, quantityField);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create book");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        panel.add(submitButton, gbc);

        return panel;
    }

    private JPanel searchManagePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Search bar
        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(30);
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Table setup
        String[] columns = { "ISBN", "Title", "Author", "Year", "Price", "Quantity" };
        tableModel = new DefaultTableModel(columns, 0);
        booksTable = new JTable(tableModel);
        updateTable(service.searchBooks("")); // Load all books by default

        JScrollPane scrollPane = new JScrollPane(booksTable);

        // Action buttons
        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("View Details");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");

        // Search action
        searchButton.addActionListener(e -> {
            String query = searchField.getText();
            List<Book> results = service.searchBooks(query);
            updateTable(results);
        });

        // View details action
        viewButton.addActionListener(e -> showBookDetails());

        // Delete action
        deleteButton.addActionListener(e -> deleteSelectedBook());

        buttonPanel.add(viewButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel bulkUploadPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton selectFileButton = new JButton("Select CSV File");
        JLabel fileLabel = new JLabel("No file selected");

        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getPath();
                fileLabel.setText(filePath);
                processCSV(filePath);
            }
        });

        panel.add(selectFileButton, BorderLayout.NORTH);
        panel.add(fileLabel, BorderLayout.CENTER);
        return panel;
    }

    // Helper methods remain the same
    private void addFormRow(JPanel panel, GridBagConstraints gbc,
                            String label, JComponent field, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void clearFields(JTextField... fields) {
        for (JTextField field : fields) {
            field.setText("");
        }
    }

    private void updateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book book : books) {
            tableModel.addRow(new Object[] {
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYear(),
                    book.getPrice(),
                    book.getQuantity()
            });
        }
    }

    private void showBookDetails() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow >= 0) {
            String isbn = (String) tableModel.getValueAt(selectedRow, 0);
            Book book = service.readBook(isbn);
            if (book != null) {
                JOptionPane.showMessageDialog(this,
                        "ISBN: " + book.getIsbn() + "\n" +
                                "Title: " + book.getTitle() + "\n" +
                                "Author: " + book.getAuthor() + "\n" +
                                "Year: " + book.getYear() + "\n" +
                                "Price: " + book.getPrice() + "\n" +
                                "Quantity: " + book.getQuantity());
            }
        }
    }

    private void deleteSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow >= 0) {
            String isbn = (String) tableModel.getValueAt(selectedRow, 0);
            boolean success = service.deleteBook(isbn);
            if (success) {
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Book deleted successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete book");
            }
        }
    }

    private void processCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<Book> books = new ArrayList<>();
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                Book book = new Book();
                book.setIsbn(nextLine[0]);
                book.setTitle(nextLine[1]);
                book.setAuthor(nextLine[2]);
                book.setYear(Integer.parseInt(nextLine[3]));
                book.setPrice(Double.parseDouble(nextLine[4]));
                book.setQuantity(Integer.parseInt(nextLine[5]));
                books.add(book);
            }

            boolean success = service.bulkUploadBooks(books);
            if (success) {
                JOptionPane.showMessageDialog(this, "Bulk upload successful!");
                updateTable(service.searchBooks("")); // Refresh table
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format in CSV");
        } catch (IOException | CsvValidationException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminClient());
    }
}