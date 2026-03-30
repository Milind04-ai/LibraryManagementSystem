import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * ReturnPanel – FULLY WIRED to the database.
 *
 * memberType = "Student"  uses: student_issue, student_fine tables
 * memberType = "Faculty"  uses: faculty_issue,  faculty_fine  tables
 *
 * Fine rates: Rs.2/day for Student,  Rs.5/day for Faculty
 */
public class ReturnPanel extends JPanel {

    private final String memberType;
    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTextField txtIssueId;
    private JTextField txtMemberId, txtMemberName, txtMemberDept;
    private JTextField txtBookId, txtBookTitle;
    private JTextField txtIssueDate, txtDueDate, txtReturnDate;
    private JLabel     lblStatus, lblOverdueDays, lblFine;
    private JComboBox<String> cmbFineCollected;

    // Hold the raw due date from DB for fine calculation
    private LocalDate parsedDueDate = null;

    private static final Color BG       = new Color(238, 242, 250);
    private static final Color C_BLUE   = new Color(25,  75, 140);
    private static final Color C_GREEN  = new Color(30, 120,  60);
    private static final Color C_RED    = new Color(185,  35,  35);
    private static final Color RONLY_BG = new Color(238, 240, 245);
    private static final Color BORDER   = new Color(200, 205, 215);

    public ReturnPanel(String memberType) {
        this.memberType = memberType;
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(18, 20, 18, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel lblTitle = new JLabel("Return Book  -  " + memberType);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(C_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(buildDetailsCard());
        row.add(buildFineCard());
        add(row, BorderLayout.CENTER);
    }

    private JPanel buildDetailsCard() {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        // Issue ID search
        g.gridx = 0; g.gridy = 0; g.weightx = 0; g.gridwidth = 1;
        card.add(lbl("Issue ID:"), g);
        g.gridx = 1; g.weightx = 1;
        txtIssueId = inputField(150, 30);
        card.add(txtIssueId, g);
        g.gridx = 2; g.weightx = 0;
        JButton btnSearch = smallBtn("Search", C_BLUE);
        card.add(btnSearch, g);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 3;
        card.add(new JSeparator(), g); g.gridwidth = 1;

        g.gridx = 0; g.gridy = 2; g.gridwidth = 3;
        JLabel sec = new JLabel(memberType + " & Book Information");
        sec.setFont(new Font("SansSerif", Font.BOLD, 13)); sec.setForeground(C_BLUE);
        card.add(sec, g); g.gridwidth = 1;

        addRORow(card, g, 3,  memberType + " ID:", txtMemberId   = roField());
        addRORow(card, g, 4,  "Name:",             txtMemberName = roField());
        addRORow(card, g, 5,  "Department:",       txtMemberDept = roField());
        addRORow(card, g, 6,  "Book ID:",          txtBookId     = roField());
        addRORow(card, g, 7,  "Book Title:",       txtBookTitle  = roField());
        addRORow(card, g, 8,  "Issue Date:",       txtIssueDate  = roField());
        addRORow(card, g, 9,  "Due Date:",         txtDueDate    = roField());

        btnSearch.addActionListener(e -> fetchRecord());
        txtIssueId.addActionListener(e -> fetchRecord());
        return card;
    }

    private JPanel buildFineCard() {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel sec = new JLabel("Return & Fine Details");
        sec.setFont(new Font("SansSerif", Font.BOLD, 13)); sec.setForeground(C_BLUE);
        card.add(sec, g); g.gridwidth = 1;

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        card.add(lbl("Return Date:"), g);
        g.gridx = 1; g.weightx = 1;
        txtReturnDate = roField();
        txtReturnDate.setText(LocalDate.now().format(FMT));
        card.add(txtReturnDate, g);

        g.gridx = 0; g.gridy = 2;
        card.add(lbl("Status:"), g);
        g.gridx = 1;
        lblStatus = new JLabel("-");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 14));
        card.add(lblStatus, g);

        g.gridx = 0; g.gridy = 3;
        card.add(lbl("Overdue Days:"), g);
        g.gridx = 1;
        lblOverdueDays = new JLabel("-");
        lblOverdueDays.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(lblOverdueDays, g);

        g.gridx = 0; g.gridy = 4;
        card.add(lbl("Fine Amount:"), g);
        g.gridx = 1;
        lblFine = new JLabel("Rs. 0");
        lblFine.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblFine.setForeground(new Color(130, 130, 130));
        card.add(lblFine, g);

        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        JLabel rateNote = new JLabel("Rate: Rs.2/day (Student)  |  Rs.5/day (Faculty)");
        rateNote.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rateNote.setForeground(new Color(120, 125, 135));
        card.add(rateNote, g); g.gridwidth = 1;

        g.gridx = 0; g.gridy = 6; g.gridwidth = 2;
        card.add(new JSeparator(), g); g.gridwidth = 1;

        g.gridx = 0; g.gridy = 7;
        card.add(lbl("Fine Collected?"), g);
        g.gridx = 1;
        cmbFineCollected = new JComboBox<>(new String[]{"Yes - Collected", "No - Waived"});
        cmbFineCollected.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(cmbFineCollected, g);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        JButton btnProcess = wideBtn("Process Return", C_RED, 160, 36);
        JButton btnClear   = wideBtn("Clear", new Color(140,145,155), 100, 36);
        btnRow.add(btnProcess); btnRow.add(btnClear);

        g.gridx = 0; g.gridy = 8; g.gridwidth = 2;
        g.insets = new Insets(18, 4, 4, 4);
        card.add(btnRow, g);

        btnProcess.addActionListener(e -> processReturn());
        btnClear.addActionListener(e -> clearAll());
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // fetchRecord()
    //
    // SQL for Student:
    //   SELECT si.student_id, s.name, s.dept,
    //          si.book_id, b.title, si.issue_date, si.due_date
    //   FROM student_issue si
    //   JOIN students s ON si.student_id = s.student_id
    //   JOIN books    b ON si.book_id    = b.book_id
    //   WHERE si.issue_id = ? AND si.return_date IS NULL
    //
    // SQL for Faculty: same but with faculty_issue and faculty tables
    // ═══════════════════════════════════════════════════════════════
    private void fetchRecord() {
        String id = txtIssueId.getText().trim();
        if (id.isEmpty()) { warn("Please enter an Issue ID."); return; }

        String issueTable  = memberType.equals("Student") ? "student_issue" : "faculty_issue";
        String memberTable = memberType.equals("Student") ? "student"      : "faculty";
        String memberIdCol = memberType.equals("Student") ? "student_id"    : "faculty_id";

        String sql =
            "SELECT si." + memberIdCol + ", m.name, m.dept, " +
            "       si.book_id, b.title, si.issue_date, si.due_date " +
            "FROM " + issueTable + " si " +
            "JOIN " + memberTable + " m ON si." + memberIdCol + " = m." + memberIdCol + " " +
            "JOIN book b ON si.book_id = b.book_id " +
            "WHERE si.issue_id = ? AND si.return_date IS NULL";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtMemberId.setText(rs.getString(memberIdCol));
                txtMemberName.setText(rs.getString("name"));
                txtMemberDept.setText(rs.getString("dept"));
                txtBookId.setText(rs.getString("book_id"));
                txtBookTitle.setText(rs.getString("title"));

                // Store due date for fine calculation
                java.sql.Date issueDate = rs.getDate("issue_date");
                java.sql.Date dueDate   = rs.getDate("due_date");
                parsedDueDate = dueDate.toLocalDate();

                txtIssueDate.setText(issueDate.toLocalDate().format(FMT));
                txtDueDate.setText(parsedDueDate.format(FMT));

                calcFine(); // auto-calculate fine immediately
            } else {
                clearInfo();
                warn("No active (unreturned) issue record found for ID: " + id);
            }

        } catch (SQLException e) {
            warn("DB error while fetching record:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void calcFine() {
        if (parsedDueDate == null) return;
        LocalDate today   = LocalDate.now();
        long      overdue = ChronoUnit.DAYS.between(parsedDueDate, today);
        int       rate    = memberType.equals("Faculty") ? 5 : 2;

        if (overdue <= 0) {
            lblStatus.setText("On Time");
            lblStatus.setForeground(C_GREEN);
            lblOverdueDays.setText("0");
            lblFine.setText("Rs. 0");
            lblFine.setForeground(new Color(130, 130, 130));
        } else {
            lblStatus.setText("Overdue");
            lblStatus.setForeground(C_RED);
            lblOverdueDays.setText(overdue + " days");
            lblFine.setText("Rs. " + (overdue * rate));
            lblFine.setForeground(C_RED);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // processReturn()
    //
    // Step 1: UPDATE student_issue/faculty_issue SET return_date = today
    // Step 2: INSERT INTO student_fine/faculty_fine (fine record)
    // Step 3: UPDATE books SET available_copies = available_copies + 1
    //
    // All three steps run in one transaction.
    // ═══════════════════════════════════════════════════════════════
    private void processReturn() {
        if (txtMemberName.getText().trim().isEmpty()) {
            warn("Please search an issue record first."); return;
        }

        boolean fineCollected = ((String) cmbFineCollected.getSelectedItem()).startsWith("Yes");
        long fineAmount = 0;

        if (parsedDueDate != null) {
            long overdue = ChronoUnit.DAYS.between(parsedDueDate, LocalDate.now());
            int  rate    = memberType.equals("Faculty") ? 5 : 2;
            fineAmount   = Math.max(0, overdue * rate);
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "<html>" +
            "Return: <b>" + txtBookTitle.getText() + "</b><br>" +
            "From:   <b>" + txtMemberName.getText() + "</b><br>" +
            "Fine:   <b>" + lblFine.getText() + "</b>  -  " +
            (fineCollected ? "Collected" : "Waived") +
            "</html>",
            "Confirm Return", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        String issueTable  = memberType.equals("Student") ? "student_issue" : "faculty_issue";
        String fineTable   = memberType.equals("Student") ? "student_fine"  : "faculty_fine";
        String memberIdCol = memberType.equals("Student") ? "student_id"    : "faculty_id";

        String sqlReturn = "UPDATE " + issueTable +
                           " SET return_date = ? WHERE issue_id = ?";
        String sqlFine   = "INSERT INTO " + fineTable +
                           " (issue_id, " + memberIdCol + ", fine_amount, paid, fine_date) " +
                           " VALUES (?, ?, ?, ?, ?)";
        String sqlBooks  = "UPDATE book SET available_copies = available_copies + 1 WHERE book_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // start transaction

            // Step 1: Mark as returned
            PreparedStatement psReturn = conn.prepareStatement(sqlReturn);
            psReturn.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            psReturn.setString(2, txtIssueId.getText().trim());
            psReturn.executeUpdate();

            // Step 2: Record fine (even if Rs.0 – keeps audit trail)
            PreparedStatement psFine = conn.prepareStatement(sqlFine);
            psFine.setString(1, txtIssueId.getText().trim());
            psFine.setString(2, txtMemberId.getText().trim());
            psFine.setLong(3,   fineAmount);
            psFine.setInt(4,    fineCollected ? 1 : 0); // 1 = paid, 0 = not paid
            psFine.setDate(5,   java.sql.Date.valueOf(LocalDate.now()));
            psFine.executeUpdate();

            // Step 3: Increase available copies
            PreparedStatement psBooks = conn.prepareStatement(sqlBooks);
            psBooks.setString(1, txtBookId.getText().trim());
            psBooks.executeUpdate();

            conn.commit(); // all three succeeded

            JOptionPane.showMessageDialog(this,
                "Return processed successfully!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
            clearAll();

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(this,
                "Error processing return:\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    private void clearAll() {
        txtIssueId.setText(""); clearInfo();
        txtReturnDate.setText(LocalDate.now().format(FMT));
        parsedDueDate = null;
        txtIssueId.requestFocusInWindow();
    }

    private void clearInfo() {
        txtMemberId.setText(""); txtMemberName.setText(""); txtMemberDept.setText("");
        txtBookId.setText("");   txtBookTitle.setText("");
        txtIssueDate.setText(""); txtDueDate.setText("");
        lblStatus.setText("-");       lblStatus.setForeground(Color.DARK_GRAY);
        lblOverdueDays.setText("-");
        lblFine.setText("Rs. 0");     lblFine.setForeground(new Color(130,130,130));
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    // ─── Layout helpers ───────────────────────────────────────────
    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(5, 5, 5, 5); return g;
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
        l.setPreferredSize(new Dimension(130, 28)); return l;
    }
    private JTextField inputField(int w, int h) {
        JTextField f = new JTextField(); f.setBackground(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(3,7,3,7)));
        f.setPreferredSize(new Dimension(w, h));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_BLUE,2), BorderFactory.createEmptyBorder(2,6,2,6)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(3,7,3,7)));
            }
        });
        return f;
    }
    private JTextField roField() {
        JTextField f = new JTextField(); f.setEditable(false); f.setBackground(RONLY_BG);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(3,7,3,7)));
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
