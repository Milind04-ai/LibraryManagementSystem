import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * BooksPanel – FULLY WIRED to the database.
 *
 * This file shows you exactly how to replace every stub in the
 * original code with real JDBC calls. Use this as a template
 * for wiring the other panels (IssuePanel, ReturnPanel, etc.).
 *
 * DB table used:
 *   CREATE TABLE books (
 *       book_id          VARCHAR(10)  PRIMARY KEY,
 *       title            VARCHAR(200) NOT NULL,
 *       author           VARCHAR(100),
 *       category         VARCHAR(50),
 *       isbn             VARCHAR(20),
 *       total_copies     INT DEFAULT 1,
 *       available_copies INT DEFAULT 1
 *   );
 */
public class BooksPanel extends JPanel {

    private JTable           table;
    private DefaultTableModel tableModel;
    private JTextField        txtSearch;
    private JComboBox<String> cmbSearchBy;
    private String            currentUser;
    private String            currentRole;

    private static final String[] COLUMNS = {
        "Book ID", "Title", "Author", "Category", "ISBN", "Total Copies", "Available"
    };

    public BooksPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
        loadBooks("", ""); // load all books on open
    }

    private void initUI() {
        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        searchPanel.setBackground(new Color(245, 247, 250));
        searchPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 210, 210)));

        searchPanel.add(new JLabel("Search By:"));
        cmbSearchBy = new JComboBox<>(new String[]{"Title", "Author", "Category", "ISBN"});
        cmbSearchBy.setPreferredSize(new Dimension(110, 28));
        searchPanel.add(cmbSearchBy);

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 28));
        searchPanel.add(txtSearch);

        JButton btnSearch  = makeBtn("Search",  new Color(33, 87, 145));
        JButton btnRefresh = makeBtn("Refresh", new Color(100, 100, 100));
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        add(searchPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        // Highlight Available column: red if 0, green otherwise
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    try {
                        int av = Integer.parseInt(val.toString());
                        setForeground(av == 0 ? new Color(185,35,35) : new Color(30,120,60));
                        setFont(getFont().deriveFont(Font.BOLD));
                        setBackground(row % 2 == 0 ? Color.WHITE : new Color(248,250,255));
                    } catch (Exception ignored) {}
                }
                return this;
            }
        });

        int[] widths = {60, 220, 160, 120, 130, 90, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(scroll, BorderLayout.CENTER);

        // Wire actions
        btnSearch.addActionListener(e  -> doSearch());
        txtSearch.addActionListener(e  -> doSearch());
        btnRefresh.addActionListener(e -> loadBooks("", ""));
    }

    // ═══════════════════════════════════════════════════════════════
    // loadBooks()  –  SELECT * FROM books  (with optional search filter)
    //
    // searchColumn: the column name to filter on ("title", "author" etc.)
    // keyword:      the value to search for (empty = no filter = all rows)
    // ═══════════════════════════════════════════════════════════════
    private void loadBooks(String searchColumn, String keyword) {
        tableModel.setRowCount(0); // clear old data

        // Build SQL – use LIKE for search, no WHERE clause for full load
        String sql;
        if (keyword.isEmpty()) {
            sql = "SELECT book_id, title, author, category, isbn, total_copies, available_copies " +
                  "FROM book ORDER BY title";
        } else {
            sql = "SELECT book_id, title, author, category, isbn, total_copies, available_copies " +
                  "FROM book WHERE " + searchColumn + " LIKE ? ORDER BY title";
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!keyword.isEmpty()) {
                ps.setString(1, "%" + keyword + "%"); // LIKE %keyword%
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getString("isbn"),
                    rs.getInt("total_copies"),
                    rs.getInt("available_copies")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading books:\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void doSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadBooks("", "");
            return;
        }
        // Map combo selection to the actual column name in your DB table
        String[] columnNames = {"title", "author", "category", "isbn"};
        String dbColumn = columnNames[cmbSearchBy.getSelectedIndex()];
        loadBooks(dbColumn, keyword);
    }

    // ─── Style helpers ────────────────────────────────────────────
    private void styleTable(JTable t) {
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        t.getTableHeader().setBackground(new Color(33, 87, 145));
        t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(180, 210, 255));
        t.setGridColor(new Color(220, 220, 220));
        t.setAutoCreateRowSorter(true);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(248,250,255));
                setBorder(new EmptyBorder(0, 5, 0, 5));
                return this;
            }
        });
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(90, 28));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
