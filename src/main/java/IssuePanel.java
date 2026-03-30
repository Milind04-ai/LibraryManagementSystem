import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * IssuePanel – FULLY WIRED to the database.
 *
 * memberType = "Student"  uses: students table, student_issue table
 * memberType = "Faculty"  uses: faculty  table, faculty_issue  table
 */
public class IssuePanel extends JPanel {

    private final String memberType;
    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter SQL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // MySQL date format

    // Member fields
    private JTextField txtMemberId, txtMemberName, txtMemberDept, txtMemberEmail;
    // Book fields
    private JTextField txtBookId, txtBookTitle, txtBookAuthor;
    private JLabel     lblAvail;
    // Issue fields
    private JComboBox<String> cmbDuration;
    private JTextField txtIssueDate, txtDueDate;

    private static final Color BG       = new Color(238, 242, 250);
    private static final Color C_BLUE   = new Color(25,  75, 140);
    private static final Color C_GREEN  = new Color(30, 120,  60);
    private static final Color C_RED    = new Color(185,  35,  35);
    private static final Color C_AMBER  = new Color(180, 110,   0);
    private static final Color RONLY_BG = new Color(238, 240, 245);
    private static final Color BORDER   = new Color(200, 205, 215);

    public IssuePanel(String memberType) {
        this.memberType = memberType;
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(18, 20, 18, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel lblTitle = new JLabel("Issue Book  -  " + memberType);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(C_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel topRow = new JPanel(new GridLayout(1, 2, 14, 0));
        topRow.setOpaque(false);
        topRow.add(buildMemberCard());
        topRow.add(buildBookCard());

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(topRow,           BorderLayout.NORTH);
        center.add(buildIssueCard(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildMemberCard() {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        addHead(card, g, 0, memberType + " Details");

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        card.add(lbl(memberType + " ID:"), g);
        g.gridx = 1; g.weightx = 1;
        txtMemberId = inputField(150, 30);
        card.add(txtMemberId, g);
        g.gridx = 2; g.weightx = 0;
        JButton btnSearch = smallBtn("Search", C_BLUE);
        card.add(btnSearch, g);

        addRORow(card, g, 2, "Name:",       txtMemberName  = roField());
        addRORow(card, g, 3, "Department:", txtMemberDept  = roField());
        addRORow(card, g, 4, "Email:",      txtMemberEmail = roField());

        btnSearch.addActionListener(e -> fetchMember());
        txtMemberId.addActionListener(e -> fetchMember());
        return card;
    }

    private JPanel buildBookCard() {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        addHead(card, g, 0, "Book Details");

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        card.add(lbl("Book ID:"), g);
        g.gridx = 1; g.weightx = 1;
        txtBookId = inputField(150, 30);
        card.add(txtBookId, g);
        g.gridx = 2; g.weightx = 0;
        JButton btnSearch = smallBtn("Search", C_BLUE);
        card.add(btnSearch, g);

        addRORow(card, g, 2, "Title:",  txtBookTitle  = roField());
        addRORow(card, g, 3, "Author:", txtBookAuthor = roField());

        g.gridx = 0; g.gridy = 4; g.weightx = 0; g.gridwidth = 1;
        card.add(lbl("Availability:"), g);
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        lblAvail = new JLabel("-");
        lblAvail.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblAvail.setForeground(Color.DARK_GRAY);
        card.add(lblAvail, g);
        g.gridwidth = 1;

        btnSearch.addActionListener(e -> fetchBook());
        txtBookId.addActionListener(e -> fetchBook());
        return card;
    }

    private JPanel buildIssueCard() {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        addHead(card, g, 0, "Issue Details");

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        card.add(lbl("Issue Date:"), g);
        g.gridx = 1; g.weightx = 0.4;
        txtIssueDate = roField();
        txtIssueDate.setText(LocalDate.now().format(FMT));
        txtIssueDate.setPreferredSize(new Dimension(140, 30));
        card.add(txtIssueDate, g);

        g.gridx = 2; g.weightx = 0;
        card.add(lbl("Loan Period:"), g);
        g.gridx = 3; g.weightx = 0.4;
        cmbDuration = new JComboBox<>(new String[]{"7 Days", "14 Days", "30 Days"});
        cmbDuration.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cmbDuration.setPreferredSize(new Dimension(140, 30));
        card.add(cmbDuration, g);

        g.gridx = 0; g.gridy = 2; g.weightx = 0;
        card.add(lbl("Due Date:"), g);
        g.gridx = 1; g.weightx = 1; g.gridwidth = 3;
        txtDueDate = roField();
        txtDueDate.setPreferredSize(new Dimension(140, 30));
        card.add(txtDueDate, g);
        g.gridwidth = 1;

        updateDueDate();

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);
        JButton btnIssue = wideBtn("Issue Book", C_GREEN, 160, 36);
        JButton btnClear = wideBtn("Clear", new Color(140,145,155), 100, 36);
        btnRow.add(btnIssue); btnRow.add(btnClear);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 4;
        g.insets = new Insets(16, 4, 4, 4);
        card.add(btnRow, g);

        cmbDuration.addActionListener(e -> updateDueDate());
        btnIssue.addActionListener(e -> processIssue());
        btnClear.addActionListener(e -> clearAll());
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // fetchMember()
    // SQL: SELECT name, dept, email FROM students WHERE student_id = ?
    //  or  SELECT name, dept, email FROM faculty  WHERE faculty_id = ?
    // ═══════════════════════════════════════════════════════════════
    private void fetchMember() {
        String id = txtMemberId.getText().trim();
        if (id.isEmpty()) { warn("Please enter a " + memberType + " ID."); return; }

        // Choose the right table and ID column based on member type
        String table   = memberType.equals("Student") ? "student" : "faculty";
        String idCol   = memberType.equals("Student") ? "student_id" : "faculty_id";
        String sql     = "SELECT name, dept, email FROM " + table + " WHERE " + idCol + " = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Found – fill in the display fields
                txtMemberName.setText(rs.getString("name"));
                txtMemberDept.setText(rs.getString("dept"));
                txtMemberEmail.setText(rs.getString("email"));
            } else {
                // Not found – clear display fields and show warning
                txtMemberName.setText("");
                txtMemberDept.setText("");
                txtMemberEmail.setText("");
                warn(memberType + " ID \"" + id + "\" not found in database.");
            }

        } catch (SQLException e) {
            warn("DB error while searching member:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // fetchBook()
    // SQL: SELECT title, author, available_copies FROM books WHERE book_id = ?
    // ═══════════════════════════════════════════════════════════════
    private void fetchBook() {
        String id = txtBookId.getText().trim();
        if (id.isEmpty()) { warn("Please enter a Book ID."); return; }

        String sql = "SELECT title, author, available_copies FROM book WHERE book_id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtBookTitle.setText(rs.getString("title"));
                txtBookAuthor.setText(rs.getString("author"));
                int avail = rs.getInt("available_copies");
                if (avail > 0) {
                    lblAvail.setText("Available - " + avail + " copies");
                    lblAvail.setForeground(C_GREEN);
                } else {
                    lblAvail.setText("Not Available (0 copies)");
                    lblAvail.setForeground(C_RED);
                }
            } else {
                txtBookTitle.setText("");
                txtBookAuthor.setText("");
                lblAvail.setText("Book not found");
                lblAvail.setForeground(C_AMBER);
            }

        } catch (SQLException e) {
            warn("DB error while searching book:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // processIssue()
    //
    // Step 1: Generate a new unique issue_id
    // Step 2: INSERT into student_issue or faculty_issue
    // Step 3: UPDATE books to reduce available_copies by 1
    //
    // Both steps run inside one transaction so either both succeed
    // or both are rolled back (no partial data).
    // ═══════════════════════════════════════════════════════════════
private void processIssue() {

        // ── Step 1: Validate that member and book have been searched ──
        if (txtMemberName.getText().trim().isEmpty()) {
            warn("Search and verify the " + memberType + " first.");
            return;
        }
        if (txtBookTitle.getText().trim().isEmpty()) {
            warn("Search and verify the Book first.");
            return;
        }
        String avail = lblAvail.getText();
        if (avail.startsWith("Not") || avail.startsWith("Book not")) {
            warn("This book is not available for issue.");
            return;
        }

        // ── Step 2: Confirm with admin before saving ──────────────────
        int ok = JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            "<html>Issue &nbsp;<b>" + txtBookTitle.getText() + "</b><br>" +
            "to &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>" + memberType + ": " +
            txtMemberName.getText() + "</b><br>" +
            "Due &nbsp;&nbsp;&nbsp;&nbsp;<b>" + txtDueDate.getText() + "</b></html>",
            "Confirm Issue", JOptionPane.YES_NO_OPTION);

        if (ok != JOptionPane.YES_OPTION) return;

        // ── Step 3: Decide which table to use based on member type ────
        //    Student → student_issue table, issue_id format: IS0001
        //    Faculty → faculty_issue  table, issue_id format: IF0001
        String issueTable  = memberType.equals("Student") ? "student_issue"  : "faculty_issue";
        String memberIdCol = memberType.equals("Student") ? "student_id"     : "faculty_id";
        String prefix      = memberType.equals("Student") ? "IS"             : "IF";

        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // start transaction - all steps succeed or all roll back

            // ── Step 4: Generate the next Issue ID automatically ──────
            //    Finds the highest existing number in the issue table
            //    and adds 1.  e.g. if last was IS0005, next will be IS0006
            String newIssueId = generateIssueId(conn, issueTable, prefix);

            // ── Step 5: Convert dates from display format (dd/MM/yyyy)
            //    to MySQL format (yyyy-MM-dd) ───────────────────────────
            DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter sqlFmt     = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate   = LocalDate.parse(txtDueDate.getText().trim(), displayFmt);

            // ── Step 6: INSERT into student_issue or faculty_issue ─────
            String sqlInsert =
                "INSERT INTO " + issueTable +
                " (issue_id, " + memberIdCol + ", book_id, issue_date, due_date) " +
                "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
            psInsert.setString(1, newIssueId);
            psInsert.setString(2, txtMemberId.getText().trim());
            psInsert.setString(3, txtBookId.getText().trim());
            psInsert.setString(4, issueDate.format(sqlFmt));
            psInsert.setString(5, dueDate.format(sqlFmt));
            psInsert.executeUpdate();

            // ── Step 7: Decrease available_copies in books table ───────
            String sqlUpdate =
                "UPDATE book " +
                "SET available_copies = available_copies - 1 " +
                "WHERE book_id = ?";

            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
            psUpdate.setString(1, txtBookId.getText().trim());
            psUpdate.executeUpdate();

            // ── Step 8: Commit both changes together ───────────────────
            conn.commit();

            // ── Step 9: Show success with the generated Issue ID ───────
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "<html>Book issued successfully!<br>" +
                "Issue ID : <b>" + newIssueId + "</b><br>" +
                "Please note this ID for return.</html>",
                "Success", JOptionPane.INFORMATION_MESSAGE);

            clearAll();

        } catch (SQLException e) {
            // If anything fails, roll back everything so no partial data is saved
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Error issuing book:\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Always close the connection when done
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }


// ── ADD THIS NEW METHOD below processIssue() ─────────────────────
// This generates the next Issue ID like IS0001, IS0002 ... or IF0001, IF0002 ...
// Paste this as a separate method, not inside processIssue()

    /**
     * Generates the next Issue ID for the given table.
     *
     * How it works:
     *   1. Finds the highest existing issue_id number in the table
     *      e.g. if rows are IS0001, IS0003, IS0007 → max number = 7
     *   2. Adds 1  →  next number = 8
     *   3. Formats with leading zeros to 4 digits  →  IS0008
     *
     * If the table is empty, starts from 1  →  IS0001 or IF0001
     */
    private String generateIssueId(Connection conn, String issueTable, String prefix)
            throws SQLException {

        // Extract just the numeric part of existing IDs and find the max
        // e.g. for IS0007, SUBSTRING(issue_id, 3) gives "0007", CAST gives 7
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(issue_id, " +
                     (prefix.length() + 1) +  // skip the prefix letters (IS or IF = 2 chars)
                     ") AS UNSIGNED)), 0) AS max_num FROM " + issueTable;

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        int nextNum = 1; // default if table is empty
        if (rs.next()) {
            nextNum = rs.getInt("max_num") + 1;
        }

        // Format: prefix + 4-digit number with leading zeros
        // IS + 0001 = IS0001,  IS + 0012 = IS0012,  IS + 0100 = IS0100
        return prefix + String.format("%04d", nextNum);
    }
    
    private void updateDueDate() {
        if (cmbDuration == null || txtDueDate == null) return;
        String sel  = (String) cmbDuration.getSelectedItem();
        int    days = sel.startsWith("7") ? 7 : sel.startsWith("14") ? 14 : 30;
        txtDueDate.setText(LocalDate.now().plusDays(days).format(FMT));
    }

    private void clearAll() {
        txtMemberId.setText(""); txtMemberName.setText("");
        txtMemberDept.setText(""); txtMemberEmail.setText("");
        txtBookId.setText(""); txtBookTitle.setText(""); txtBookAuthor.setText("");
        lblAvail.setText("-"); lblAvail.setForeground(Color.DARK_GRAY);
        if (cmbDuration != null) cmbDuration.setSelectedIndex(0);
        updateDueDate();
        txtMemberId.requestFocusInWindow();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    // ─── Layout helpers ───────────────────────────────────────────
    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(6, 5, 6, 5);
        return g;
    }
    private void addHead(JPanel p, GridBagConstraints g, int row, String text) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 3; g.weightx = 1;
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13)); l.setForeground(C_BLUE);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        p.add(l, g); g.gridwidth = 1;
    }
    private void addRORow(JPanel p, GridBagConstraints g, int row, String labelTxt, JTextField f) {
        g.gridx = 0; g.gridy = row; g.weightx = 0; g.gridwidth = 1; p.add(lbl(labelTxt), g);
        g.gridx = 1; g.weightx = 1; g.gridwidth = 2; p.add(f, g); g.gridwidth = 1;
    }
    private JPanel makeCard() {
        JPanel p = new JPanel(); p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), new EmptyBorder(16, 20, 16, 20)));
        return p;
    }
    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setPreferredSize(new Dimension(120, 28)); return l;
    }
    private JTextField inputField(int w, int h) {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 13)); f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(3, 7, 3, 7)));
        f.setPreferredSize(new Dimension(w, h));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_BLUE, 2), BorderFactory.createEmptyBorder(2,6,2,6)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(3,7,3,7)));
            }
        });
        return f;
    }
    private JTextField roField() {
        JTextField f = new JTextField(); f.setEditable(false);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13)); f.setBackground(RONLY_BG);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(3, 7, 3, 7)));
        f.setPreferredSize(new Dimension(180, 30)); return f;
    }
    private JButton smallBtn(String t, Color bg) {
        JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12)); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(82, 28));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton wideBtn(String t, Color bg, int w, int h) {
        JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13)); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(w, h));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
}
