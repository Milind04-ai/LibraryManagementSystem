import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * IssuedRecordsFrame  –  View all issued books (Student + Faculty combined).
 * Filter by member type and status. Call getContentPanel() to embed.
 *
 * TODO connections:
 *   loadData() → UNION query on student_issue JOIN students JOIN books
 *                            AND faculty_issue  JOIN faculty  JOIN books
 */
public class IssuedRecordsFrame extends JFrame {

    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> cmbType, cmbStatus;
    private JTextField txtSearch;
    private JLabel lblCount;

    private static final String[] COLS = {
        "Issue ID","Member ID","Member Name","Type",
        "Book ID","Book Title","Issue Date","Due Date","Return Date","Status"
    };

    private static final Color BG     = new Color(238, 242, 250);
    private static final Color C_BLUE = new Color(25,  75, 140);
    private static final Color C_GREEN= new Color(30, 120, 60);
    private static final Color C_RED  = new Color(185, 35,  35);

    public IssuedRecordsFrame() {
        setTitle("All Issued Books");
        setSize(1050, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public JPanel getContentPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Title
        JLabel lblTitle = new JLabel("All Issued Books");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(C_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        // Filter bar
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 215)),
            new EmptyBorder(2, 4, 2, 4)));

        bar.add(makeLabel("Member Type:"));
        cmbType = new JComboBox<>(new String[]{"All","Student","Faculty"});
        cmbType.setPreferredSize(new Dimension(110, 27));
        cmbType.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bar.add(cmbType);

        bar.add(makeLabel("Status:"));
        cmbStatus = new JComboBox<>(new String[]{"All","Issued","Overdue","Returned"});
        cmbStatus.setPreferredSize(new Dimension(110, 27));
        cmbStatus.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bar.add(cmbStatus);

        bar.add(makeLabel("Search:"));
        txtSearch = makeTextField(180, 27);
        bar.add(txtSearch);

        JButton btnApply   = makeBtn("Apply",   C_BLUE, 80, 27);
        JButton btnRefresh = makeBtn("Refresh", new Color(100,105,115), 80, 27);
        bar.add(btnApply);
        bar.add(btnRefresh);

        bar.add(Box.createHorizontalStrut(10));
        lblCount = new JLabel();
        lblCount.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblCount.setForeground(new Color(110, 115, 125));
        bar.add(lblCount);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        // Type column colour
        table.getColumnModel().getColumn(3).setCellRenderer(typeRenderer());
        // Status column colour
        table.getColumnModel().getColumn(9).setCellRenderer(statusRenderer());

        int[] widths = {70, 80, 150, 70, 65, 200, 88, 88, 88, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 205, 215)));

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(bar,    BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // Listeners
        btnApply.addActionListener(e -> applyFilter());
        txtSearch.addActionListener(e -> applyFilter());
        btnRefresh.addActionListener(e -> {
            cmbType.setSelectedIndex(0);
            cmbStatus.setSelectedIndex(0);
            txtSearch.setText("");
            table.setRowSorter(null);
            loadData("All", "All", "");
        });

        loadData("All", "All", "");
        return root;
    }

    private void applyFilter() {
        loadData(
            (String) cmbType.getSelectedItem(),
            (String) cmbStatus.getSelectedItem(),
            txtSearch.getText().trim()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // TODO: Build SQL with WHERE clauses based on filter values.
    //
    //   SELECT 'Student' AS type,
    //          si.issue_id, si.student_id, s.name,
    //          si.book_id, b.title,
    //          si.issue_date, si.due_date, si.return_date,
    //          CASE WHEN si.return_date IS NOT NULL THEN 'Returned'
    //               WHEN si.due_date < CURDATE()   THEN 'Overdue'
    //               ELSE 'Issued' END AS status
    //   FROM student_issue si
    //   JOIN students s ON si.student_id = s.student_id
    //   JOIN books    b ON si.book_id    = b.book_id
    //   UNION ALL
    //   SELECT 'Faculty', fi.issue_id, fi.faculty_id, f.name,
    //          fi.book_id, b.title,
    //          fi.issue_date, fi.due_date, fi.return_date,
    //          CASE ... END
    //   FROM faculty_issue fi
    //   JOIN faculty f ON fi.faculty_id = f.faculty_id
    //   JOIN books   b ON fi.book_id    = b.book_id
    //   ORDER BY issue_date DESC
    // ─────────────────────────────────────────────────────────────
    private void loadData(String type, String status, String search) {
    tableModel.setRowCount(0);
    table.setRowSorter(null);

    // Build the Student part of the query
    String studentSQL =
        "SELECT 'Student' AS type, si.issue_id, si.student_id AS member_id, s.name, " +
        "       si.book_id, b.title, si.issue_date, si.due_date, si.return_date, " +
        "       CASE WHEN si.return_date IS NOT NULL THEN 'Returned' " +
        "            WHEN si.due_date < CURDATE() THEN 'Overdue' " +
        "            ELSE 'Issued' END AS status " +
        "FROM student_issue si " +
        "JOIN student s ON si.student_id = s.student_id " +
        "JOIN book    b ON si.book_id    = b.book_id ";

    // Build the Faculty part of the query
    String facultySQL =
        "SELECT 'Faculty' AS type, fi.issue_id, fi.faculty_id AS member_id, f.name, " +
        "       fi.book_id, b.title, fi.issue_date, fi.due_date, fi.return_date, " +
        "       CASE WHEN fi.return_date IS NOT NULL THEN 'Returned' " +
        "            WHEN fi.due_date < CURDATE() THEN 'Overdue' " +
        "            ELSE 'Issued' END AS status " +
        "FROM faculty_issue fi " +
        "JOIN faculty f ON fi.faculty_id = f.faculty_id " +
        "JOIN book   b ON fi.book_id    = b.book_id ";

    // Decide which SQL to run based on the Type filter dropdown
    String sql;
    if (type.equals("Student")) {
        sql = studentSQL + "ORDER BY si.issue_date DESC";
    } else if (type.equals("Faculty")) {
        sql = facultySQL + "ORDER BY fi.issue_date DESC";
    } else {
        // "All" - combine both with UNION ALL
        sql = studentSQL + "UNION ALL " + facultySQL + "ORDER BY issue_date DESC";
    }

    try (Connection conn = DatabaseHelper.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            // Apply Status filter (Issued / Overdue / Returned / All)
            String rowStatus = rs.getString("status");
            if (!status.equals("All") && !rowStatus.equals(status)) {
                continue; // skip rows that don't match status filter
            }

            // Apply Search filter (searches member name and book title)
            String name  = rs.getString("name") != null  ? rs.getString("name")  : "";
            String title = rs.getString("title") != null ? rs.getString("title") : "";
            if (!search.isEmpty() &&
                !name.toLowerCase().contains(search.toLowerCase()) &&
                !title.toLowerCase().contains(search.toLowerCase())) {
                continue; // skip rows that don't match search
            }

            // Format return date - show dash if book not yet returned
            String returnDate = rs.getString("return_date");
            String returnDisplay = (returnDate != null) ? returnDate : "—";

            tableModel.addRow(new Object[]{
                rs.getString("issue_id"),    // Issue ID
                rs.getString("member_id"),   // Member ID
                rs.getString("name"),        // Member Name
                rs.getString("type"),        // Type (Student/Faculty)
                rs.getString("book_id"),     // Book ID
                rs.getString("title"),       // Book Title
                rs.getString("issue_date"),  // Issue Date
                rs.getString("due_date"),    // Due Date
                returnDisplay,               // Return Date
                rs.getString("status")       // Status
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Error loading records:\n" + e.getMessage(),
            "DB Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    lblCount.setText("Showing " + tableModel.getRowCount() + " records");
}

    // ─── Renderers ────────────────────────────────────────────────
    private DefaultTableCellRenderer typeRenderer() {
        return new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String s = v != null ? v.toString() : "";
                if (!sel) {
                    setBackground(s.equals("Faculty")
                        ? new Color(240, 235, 255) : row % 2 == 0 ? Color.WHITE : new Color(248,250,255));
                    setForeground(s.equals("Faculty")
                        ? new Color(80, 60, 160) : C_BLUE);
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                }
                return this;
            }
        };
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String s = v != null ? v.toString() : "";
                if (!sel) {
                    setBackground(s.equals("Overdue")  ? new Color(255,228,228) :
                                  s.equals("Returned") ? new Color(225,248,228) :
                                  row % 2 == 0 ? Color.WHITE : new Color(248,250,255));
                    setForeground(s.equals("Overdue")  ? C_RED   :
                                  s.equals("Returned") ? C_GREEN : C_BLUE);
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                }
                return this;
            }
        };
    }

    // ─── Style helpers ────────────────────────────────────────────
    private void styleTable(JTable t) {
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(26);
        t.setGridColor(new Color(210, 215, 225));
        t.setSelectionBackground(new Color(185, 210, 255));
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        t.getTableHeader().setBackground(C_BLUE);
        t.getTableHeader().setForeground(Color.WHITE);
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

    private JButton makeBtn(String text, Color bg, int w, int h) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(w, h));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel makeLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    private JTextField makeTextField(int w, int h) {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBackground(new Color(248, 249, 251));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 215)),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        f.setPreferredSize(new Dimension(w, h));
        return f;
    }
}
