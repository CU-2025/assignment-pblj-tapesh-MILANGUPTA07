import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class LibraryManagerApp extends JFrame implements ActionListener {
    private JTextField[] fields = new JTextField[7];
    private JComboBox<String> categoryCombo;
    private JButton addButton, viewButton, editButton, deleteButton, clearButton, exitButton;
    private JButton addCategoryButton, deleteCategoryButton, uploadFileButton;
    private JLabel uploadedFileLabel;
    private File selectedBookFile = null;
    private ArrayList<String[]> books = new ArrayList<>();
    private ArrayList<String> categories = new ArrayList<>(Arrays.asList(
        "Philosophy", "Sports", "Science", "Literature", "History", "Technology"
    ));
    private JPanel formPanel, buttonPanel;
    private JLabel titleLabel;

    private String[] labels = {
        "Book ID", "Book Title", "Author", "Publisher",
        "Year of Publication", "ISBN", "Number of Copies", "Category", "Book File"
    };

    // --- Gradient Background Panel ---
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            // Multi-stop gradient: cyan -> blue -> purple/pink
            Color color1 = new Color(0, 255, 255);      // Cyan (top-left)
            Color color2 = new Color(0, 102, 255);      // Blue (center)
            Color color3 = new Color(186, 42, 186);     // Purple/Pink (bottom-right)
            GradientPaint gp1 = new GradientPaint(0, 0, color1, w, h/2, color2);
            g2d.setPaint(gp1);
            g2d.fillRect(0, 0, w, h);
            GradientPaint gp2 = new GradientPaint(w/2, h/2, color2, w, h, color3);
            g2d.setPaint(gp2);
            g2d.fillRect(0, h/2, w, h/2);
        }
    }

    public LibraryManagerApp() {
        setTitle("📚 Library Management System");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        // --- Main panel with gradient background ---
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(52,152,219), 2, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Title label
        titleLabel = new JLabel("BookNest", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);  // Contrast on gradient
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        formPanel = new JPanel(new GridLayout(12, 2, 12, 12));
        formPanel.setBackground(new Color(255,255,255,230)); // Slightly transparent white
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(41,128,185), 2),
            "Book Details",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 18),
            new Color(41,128,185)
        ));

        for (int i = 0; i < 7; i++) {
            JLabel label = new JLabel(labels[i] + ":");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            label.setForeground(new Color(44, 62, 80));
            formPanel.add(label);

            fields[i] = new JTextField();
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 16));
            fields[i].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            addFocusHighlight(fields[i]);
            formPanel.add(fields[i]);
        }

        // Category label and combo box
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        categoryLabel.setForeground(new Color(44, 62, 80));
        formPanel.add(categoryLabel);

        categoryCombo = new JComboBox<>(categories.toArray(new String[0]));
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(categoryCombo);

        // Category add/delete buttons
        addCategoryButton = new JButton("Add Category");
        addCategoryButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addCategoryButton.setBackground(new Color(220, 240, 220));
        addCategoryButton.setFocusPainted(false);
        addCategoryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addCategoryButton.addActionListener(e -> {
            String newCategory = JOptionPane.showInputDialog(this, "Enter new category:");
            if (newCategory != null && !newCategory.trim().isEmpty()) {
                if (!categories.contains(newCategory)) {
                    categories.add(newCategory);
                    categoryCombo.addItem(newCategory);
                    showMessage("Category Added", "Category added successfully!", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showMessage("Duplicate Category", "This category already exists.", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        deleteCategoryButton = new JButton("Delete Category");
        deleteCategoryButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deleteCategoryButton.setBackground(new Color(255, 220, 220));
        deleteCategoryButton.setFocusPainted(false);
        deleteCategoryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteCategoryButton.addActionListener(e -> {
            String selectedCategory = (String) categoryCombo.getSelectedItem();
            if (selectedCategory == null) {
                showMessage("No Category Selected", "Please select a category to delete.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (String[] book : books) {
                if (book.length > 7 && selectedCategory.equals(book[7])) {
                    showMessage("Cannot Delete", "This category is assigned to at least one book.", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the category '" + selectedCategory + "'?",
                "Delete Category", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                categories.remove(selectedCategory);
                categoryCombo.removeItem(selectedCategory);
                showMessage("Category Deleted", "Category deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel categoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        categoryButtonPanel.setBackground(Color.WHITE);
        categoryButtonPanel.add(addCategoryButton);
        categoryButtonPanel.add(deleteCategoryButton);
        formPanel.add(new JLabel()); // for alignment
        formPanel.add(categoryButtonPanel);

        // File upload UI
        JLabel fileLabel = new JLabel("Upload Book File:");
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        fileLabel.setForeground(new Color(44, 62, 80));
        formPanel.add(fileLabel);

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBackground(Color.WHITE);

        uploadFileButton = new JButton("Choose File");
        uploadFileButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        uploadFileButton.setBackground(new Color(230, 230, 250));
        uploadFileButton.setFocusPainted(false);
        uploadFileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadFileButton.addActionListener(e -> chooseBookFile());
        filePanel.add(uploadFileButton, BorderLayout.WEST);

        uploadedFileLabel = new JLabel("No file selected");
        uploadedFileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        uploadedFileLabel.setForeground(new Color(120, 120, 120));
        filePanel.add(uploadedFileLabel, BorderLayout.CENTER);

        formPanel.add(filePanel);

        // Button panel
        buttonPanel = new JPanel(new GridLayout(6, 1, 12, 12));
        buttonPanel.setOpaque(false);

        // BUTTONS WITH BLACK TEXT
        addButton = createStyledButton("  Add Book", new Color(0, 204, 255));
        viewButton = createStyledButton("  View All Books", new Color(0, 102, 255));
        editButton = createStyledButton("  Edit Book", new Color(186, 42, 186));
        deleteButton = createStyledButton("  Delete Book", new Color(255, 99, 132));
        clearButton = createStyledButton("  Clear Fields", new Color(255, 206, 86));
        exitButton = createStyledButton("  Exit", new Color(54, 162, 235));

        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        // Add panels to main panel
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setVisible(true);
    }

    private void addFocusHighlight(JTextField field) {
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
    }

    private void chooseBookFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Book Files (.pdf, .epub, .mobi)", "pdf", "epub", "mobi");
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String name = file.getName().toLowerCase();
            if (!(name.endsWith(".pdf") || name.endsWith(".epub") || name.endsWith(".mobi"))) {
                showMessage("Invalid File", "Only PDF, EPUB, or MOBI files are allowed.", JOptionPane.ERROR_MESSAGE);
                selectedBookFile = null;
                uploadedFileLabel.setText("No file selected");
                return;
            }
            long maxSize = 50L * 1024 * 1024;
            if (file.length() > maxSize) {
                showMessage("File Too Large", "File size must be less than 50 MB.", JOptionPane.ERROR_MESSAGE);
                selectedBookFile = null;
                uploadedFileLabel.setText("No file selected");
                return;
            }
            selectedBookFile = file;
            uploadedFileLabel.setText(file.getName());
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK); // BLACK TEXT FOR VISIBILITY[3][4][5][7]
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker()),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(this);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        try {
            if (source == addButton) {
                if (validateFields() && validateBookFile()) {
                    String[] book = new String[9];
                    for (int i = 0; i < 7; i++) book[i] = fields[i].getText();
                    book[7] = (String) categoryCombo.getSelectedItem();
                    book[8] = (selectedBookFile != null) ? selectedBookFile.getAbsolutePath() : "";
                    boolean updated = false;
                    for (int i = 0; i < books.size(); i++) {
                        if (books.get(i)[0].equals(book[0])) {
                            books.set(i, book);
                            updated = true;
                            break;
                        }
                    }
                    if (!updated) {
                        books.add(book);
                        showMessage("Book Added", "✅ Book added successfully!", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        showMessage("Book Updated", "✏ Book updated successfully!", JOptionPane.INFORMATION_MESSAGE);
                    }
                    clearFields();
                }
            } else if (source == viewButton) {
                if (books.isEmpty()) {
                    showMessage("No Books", "There are no books in the library yet.", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JFrame viewFrame = new JFrame("📚 Book List");
                viewFrame.setSize(1200, 500);
                viewFrame.setLocationRelativeTo(this);

                String[] columns = labels;
                Object[][] data = new Object[books.size()][9];
                for (int i = 0; i < books.size(); i++) data[i] = books.get(i);

                DefaultTableModel model = new DefaultTableModel(data, columns) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                JTable table = new JTable(model);
                table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                table.setRowHeight(28);
                table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
                table.getTableHeader().setBackground(new Color(52,152,219));
                table.getTableHeader().setForeground(Color.WHITE);
                table.setSelectionBackground(new Color(220, 240, 255));
                table.setSelectionForeground(Color.BLACK);
                table.setAutoCreateRowSorter(true);

                // Double-click to open file
                table.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            int row = table.getSelectedRow();
                            if (row != -1) {
                                int modelRow = table.convertRowIndexToModel(row);
                                String filePath = (String) model.getValueAt(modelRow, columns.length - 1);
                                if (filePath != null && !filePath.isEmpty()) {
                                    File file = new File(filePath);
                                    if (file.exists()) {
                                        try {
                                            Desktop.getDesktop().open(file);
                                        } catch (Exception ex) {
                                            showMessage("Error", "Cannot open file: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                                        }
                                    } else {
                                        showMessage("File Not Found", "The file does not exist at the saved path.", JOptionPane.ERROR_MESSAGE);
                                    }
                                } else {
                                    showMessage("No File", "No file is associated with this book.", JOptionPane.WARNING_MESSAGE);
                                }
                            }
                        }
                    }
                });

                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JPanel searchPanel = new JPanel(new BorderLayout());
                JTextField searchField = new JTextField();
                searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Search Books"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));

                searchField.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        String searchText = searchField.getText().toLowerCase();
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        model.setRowCount(0);

                        for (String[] book : books) {
                            boolean match = false;
                            for (String field : book) {
                                if (field.toLowerCase().contains(searchText)) {
                                    match = true;
                                    break;
                                }
                            }
                            if (match) model.addRow(book);
                        }
                    }
                });

                searchPanel.add(searchField, BorderLayout.CENTER);

                JPanel contentPanel = new JPanel(new BorderLayout());
                contentPanel.add(searchPanel, BorderLayout.NORTH);
                contentPanel.add(scrollPane, BorderLayout.CENTER);

                viewFrame.add(contentPanel);
                viewFrame.setVisible(true);

            } else if (source == editButton) {
                String bookID = JOptionPane.showInputDialog(this,
                    "Enter Book ID to Edit:", "Edit Book", JOptionPane.QUESTION_MESSAGE);

                if (bookID != null && !bookID.trim().isEmpty()) {
                    for (int i = 0; i < books.size(); i++) {
                        if (books.get(i)[0].equals(bookID)) {
                            String[] book = books.get(i);
                            for (int j = 0; j < 7; j++) {
                                fields[j].setText(book[j]);
                            }
                            categoryCombo.setSelectedItem(book[7]);
                            selectedBookFile = (book[8].isEmpty()) ? null : new File(book[8]);
                            uploadedFileLabel.setText(selectedBookFile == null ? "No file selected" : selectedBookFile.getName());
                            showMessage("Edit Mode", "✏ Book loaded for editing. Make changes and click 'Add' to update.",
                                JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                    }
                    showMessage("Not Found", "❌ Book not found", JOptionPane.WARNING_MESSAGE);
                }

            } else if (source == deleteButton) {
                String bookID = JOptionPane.showInputDialog(this,
                    "Enter Book ID to Delete:", "Delete Book", JOptionPane.WARNING_MESSAGE);
                if (bookID != null && !bookID.trim().isEmpty()) {
                    for (int i = 0; i < books.size(); i++) {
                        if (books.get(i)[0].equals(bookID)) {
                            books.remove(i);
                            showMessage("Book Deleted", "🗑 Book deleted successfully",
                                JOptionPane.INFORMATION_MESSAGE);
                            clearFields();
                            return;
                        }
                    }
                    showMessage("Not Found", "❌ Book not found", JOptionPane.WARNING_MESSAGE);
                }

            } else if (source == clearButton) {
                clearFields();
            } else if (source == exitButton) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?", "Exit",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        } catch (Exception ex) {
            showMessage("Error", "An error occurred: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateFields() {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getText().trim().isEmpty()) {
                showMessage("Validation Error", "Please fill in all fields", JOptionPane.WARNING_MESSAGE);
                fields[i].requestFocus();
                return false;
            }
            if (i == 4 || i == 6) {
                try {
                    Integer.parseInt(fields[i].getText());
                } catch (NumberFormatException e) {
                    showMessage("Validation Error", labels[i] + " must be a number", JOptionPane.WARNING_MESSAGE);
                    fields[i].requestFocus();
                    return false;
                }
            }
        }
        if (categoryCombo.getSelectedItem() == null || ((String)categoryCombo.getSelectedItem()).trim().isEmpty()) {
            showMessage("Validation Error", "Please select a category", JOptionPane.WARNING_MESSAGE);
            categoryCombo.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateBookFile() {
        if (selectedBookFile == null) {
            showMessage("Validation Error", "Please upload a book file (.pdf, .epub, .mobi, max 50MB)", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearFields() {
        for (JTextField field : fields) {
            field.setText("");
        }
        if (categoryCombo.getItemCount() > 0)
            categoryCombo.setSelectedIndex(0);
        selectedBookFile = null;
        uploadedFileLabel.setText("No file selected");
    }

    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.PLAIN, 12));
                new LibraryManagerApp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}