import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * FinePanel  -  Fully wired to MySQL database.
 *
 * Shows 3 summary cards (Total Fine / Student Fine / Faculty Fine)
 * and two separate tables loaded from student_fine and faculty_fine tables.
 * Right-click any row to mark fine as Paid.
 *
 * DB tables used:
 *   student_fine  JOIN student_issue JOIN students JOIN books
 *   faculty_fine  JOIN faculty_issue JOIN faculty  JOIN books
 */
public class FinePanel extends JPanel {

    private JLabel valTotal, valStu, valFac;
    private DefaultTableModel stuModel, facModel;
    private JTable            stuTable, facTable;

    private static final String[] COLS = {
        "Fine ID", "Issue ID", "Member ID", "Name", "Book Title",
        "Due Date", "Return Date", "Days Late", "Fine (Rs.)", "Paid"
    };

    private static final Color BG       = new Color(238, 242, 250);
    private static final Color C_BLUE   = new Color(25,  75, 140);
    private static final Color C_GREEN  = new Color(30, 120,  60);
    private static final Color C_RED    = new Color(185,  35,  35);
    private static final Color C_AMBER  = new Color(180, 110,   0);
    private static final Color C_PURPLE = new Color(80,   60, 160);

    public FinePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(18, 20, 18, 20));
        buildUI();
        loadAll();
    }

    // ─────────────────────────────────────────────────────────────
    // UI BUILD  (unchanged from original - no edits needed here)
    // ─────────────────────────────────────────────────────────────
    private void buildUI() {
        // Title + Refresh button row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Fine Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(C_BLUE);
        titleRow.add(title, BorderLayout.WEST);

        JButton btnRefresh = btn("Refresh", C_BLUE, 100, 30);
        btnRefresh.addActionListener(e -> loadAll());
        JPanel bw = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bw.setOpaque(false);
        bw.add(btnRefresh);
        titleRow.add(bw, BorderLayout.EAST);
        add(titleRow, BorderLayout.NORTH);

        // Scrollable body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // ── 3 Summary stat cards ───────────────────────────────
        JPanel cardRow = new JPanel(new GridLayout(1, 3, 14, 0));
        cardRow.setOpaque(false);
        cardRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel c1 = statCard("Total Outstanding Fine", "—", C_RED);
        JPanel c2 = statCard("Student Fine Pending",   "—", C_AMBER);
        JPanel c3 = statCard("Faculty Fine Pending",   "—", C_PURPLE);
        valTotal = findValLabel(c1);
        valStu   = findValLabel(c2);
        valFac   = findValLabel(c3);
        cardRow.add(c1); cardRow.add(c2); cardRow.add(c3);
        body.add(cardRow);
        body.add(Box.createVerticalStrut(20));

        // ── Student fine table ─────────────────────────────────
        body.add(groupLabel("Student Fine Records   (from  student_fine  table)"));
        body.add(Box.createVerticalStrut(5));

        stuModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        stuTable = buildFineTable(stuModel, "Student");
        body.add(tableWrap(stuTable));
        body.add(Box.createVerticalStrut(18));

        // ── Faculty fine table ─────────────────────────────────
        body.add(groupLabel("Faculty Fine Records   (from  faculty_fine  table)"));
        body.add(Box.createVerticalStrut(5));

        facModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        facTable = buildFineTable(facModel, "Faculty");
        body.add(tableWrap(facTable));

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(14);
        add(bodyScroll, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────
    // loadAll()  -  calls both load methods then computes total fine
    // ─────────────────────────────────────────────────────────────
    private void loadAll() {
        loadStuFines();
        loadFacFines();
        loadFineTotals();
    }

    // ─────────────────────────────────────────────────────────────
    // loadStuFines()
    //
    // SQL:
    //   SELECT sf.fine_id, sf.issue_id, sf.student_id, s.name,
    //          b.title,
    //          si.due_date, si.return_date,
    //          DATEDIFF(IFNULL(si.return_date, CURDATE()), si.due_date) AS days_late,
    //          sf.fine_amount,
    //          IF(sf.paid = 1, 'Yes', 'No') AS paid_text
    //   FROM student_fine sf
    //   JOIN student_issue si ON sf.issue_id   = si.issue_id
    //   JOIN students      s  ON sf.student_id = s.student_id
    //   JOIN books         b  ON si.book_id    = b.book_id
    //   ORDER BY sf.paid ASC, sf.fine_date DESC
    // ─────────────────────────────────────────────────────────────
    private void loadStuFines() {
        stuModel.setRowCount(0);

        String sql = "SELECT sf.fine_id, sf.issue_id, sf.student_id, s.name, " +
                     "       b.title, " +
                     "       si.due_date, si.return_date, " +
                     "       DATEDIFF(IFNULL(si.return_date, CURDATE()), si.due_date) AS days_late, " +
                     "       sf.fine_amount, " +
                     "       IF(sf.paid = 1, 'Yes', 'No') AS paid_text " +
                     "FROM student_fine sf " +
                     "JOIN student_issue si ON sf.issue_id   = si.issue_id " +
                     "JOIN student      s  ON sf.student_id = s.student_id " +
                     "JOIN book         b  ON si.book_id    = b.book_id " +
                     "ORDER BY sf.paid ASC, sf.fine_date DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Format return_date: show "—" if book not yet returned
                String returnDate = rs.getString("return_date");
                String returnDisplay = (returnDate != null && !returnDate.equals("null"))
                                       ? returnDate : "—";

                // Format days_late: show 0 if negative (returned early)
                int daysLate = rs.getInt("days_late");
                String daysDisplay = String.valueOf(Math.max(0, daysLate));

                // Format fine amount as "Rs. X"
                double fineAmt = rs.getDouble("fine_amount");
                String fineDisplay = "Rs. " + (int) fineAmt;

                stuModel.addRow(new Object[]{
                    rs.getString("fine_id"),     // Fine ID
                    rs.getString("issue_id"),    // Issue ID
                    rs.getString("student_id"),  // Member ID
                    rs.getString("name"),        // Name
                    rs.getString("title"),       // Book Title
                    rs.getString("due_date"),    // Due Date
                    returnDisplay,               // Return Date
                    daysDisplay,                 // Days Late
                    fineDisplay,                 // Fine (Rs.)
                    rs.getString("paid_text")    // Paid (Yes/No)
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Error loading student fines:\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // loadFacFines()
    //
    // SQL: same structure as above but uses faculty_fine,
    //      faculty_issue, and faculty tables
    // ─────────────────────────────────────────────────────────────
    private void loadFacFines() {
        facModel.setRowCount(0);

        String sql = "SELECT ff.fine_id, ff.issue_id, ff.faculty_id, f.name, " +
                     "       b.title, " +
                     "       fi.due_date, fi.return_date, " +
                     "       DATEDIFF(IFNULL(fi.return_date, CURDATE()), fi.due_date) AS days_late, " +
                     "       ff.fine_amount, " +
                     "       IF(ff.paid = 1, 'Yes', 'No') AS paid_text " +
                     "FROM faculty_fine ff " +
                     "JOIN faculty_issue fi ON ff.issue_id   = fi.issue_id " +
                     "JOIN faculty       f  ON ff.faculty_id = f.faculty_id " +
                     "JOIN book         b  ON fi.book_id    = b.book_id " +
                     "ORDER BY ff.paid ASC, ff.fine_date DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String returnDate = rs.getString("return_date");
                String returnDisplay = (returnDate != null && !returnDate.equals("null"))
                                       ? returnDate : "—";

                int daysLate = rs.getInt("days_late");
                String daysDisplay = String.valueOf(Math.max(0, daysLate));

                double fineAmt = rs.getDouble("fine_amount");
                String fineDisplay = "Rs. " + (int) fineAmt;

                facModel.addRow(new Object[]{
                    rs.getString("fine_id"),     // Fine ID
                    rs.getString("issue_id"),    // Issue ID
                    rs.getString("faculty_id"),  // Member ID
                    rs.getString("name"),        // Name
                    rs.getString("title"),       // Book Title
                    rs.getString("due_date"),    // Due Date
                    returnDisplay,               // Return Date
                    daysDisplay,                 // Days Late
                    fineDisplay,                 // Fine (Rs.)
                    rs.getString("paid_text")    // Paid (Yes/No)
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Error loading faculty fines:\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // loadFineTotals()
    //
    // SQL:
    //   SELECT SUM(fine_amount) FROM student_fine WHERE paid = 0
    //   SELECT SUM(fine_amount) FROM faculty_fine  WHERE paid = 0
    // ─────────────────────────────────────────────────────────────
    private void loadFineTotals() {
        double stuTotal = 0;
        double facTotal = 0;

        // Student pending fine total
        String sqlStu = "SELECT COALESCE(SUM(fine_amount), 0) AS total " +
                        "FROM student_fine WHERE paid = 0";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlStu);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stuTotal = rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Faculty pending fine total
        String sqlFac = "SELECT COALESCE(SUM(fine_amount), 0) AS total " +
                        "FROM faculty_fine WHERE paid = 0";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlFac);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                facTotal = rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Update the 3 stat cards
        valStu.setText("Rs. " + (int) stuTotal);
        valFac.setText("Rs. " + (int) facTotal);
        valTotal.setText("Rs. " + (int)(stuTotal + facTotal));
    }

    // ─────────────────────────────────────────────────────────────
    // markPaid()
    //
    // Called when admin right-clicks a row and selects "Mark as Paid"
    //
    // SQL:
    //   UPDATE student_fine SET paid = 1 WHERE fine_id = ?
    //   UPDATE faculty_fine  SET paid = 1 WHERE fine_id = ?
    // ─────────────────────────────────────────────────────────────
    private void markPaid(JTable t, DefaultTableModel m, String type) {
        int r = t.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Please select a row first.", "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int mr = t.convertRowIndexToModel(r);

        // Check if already paid
        if ("Yes".equals(m.getValueAt(mr, 9))) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "This fine is already marked as Paid.",
                "Already Paid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String fineId  = m.getValueAt(mr, 0).toString();
        String name    = m.getValueAt(mr, 3).toString();
        String amount  = m.getValueAt(mr, 8).toString();

        int choice = JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            "<html>Mark fine of <b>" + amount + "</b> for <b>" + name + "</b> as Paid?</html>",
            "Confirm Payment", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        // Choose correct fine table
        String fineTable = type.equals("Student") ? "student_fine" : "faculty_fine";
        String sql = "UPDATE " + fineTable + " SET paid = 1 WHERE fine_id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fineId);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                // Update the table cell in UI immediately
                m.setValueAt("Yes", mr, 9);
                t.repaint();
                // Refresh totals in stat cards
                loadFineTotals();
                JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Fine marked as Paid successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Error updating fine:\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Table builder  (builds both stuTable and facTable)
    // ─────────────────────────────────────────────────────────────
    private JTable buildFineTable(DefaultTableModel model, String type) {
        JTable t = new JTable(model);
        styleTable(t);

        // Paid column renderer: green = Yes, red = No
        t.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, row, col);
                String s = v != null ? v.toString() : "";
                if (!sel) {
                    setBackground(s.equals("Yes")
                        ? new Color(225, 248, 228)
                        : row % 2 == 0 ? Color.WHITE : new Color(248, 250, 255));
                    setForeground(s.equals("Yes") ? C_GREEN : C_RED);
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                }
                return this;
            }
        });

        // Column widths
        int[] w = {58, 70, 80, 140, 178, 88, 88, 65, 74, 50};
        for (int i = 0; i < w.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        // Right-click popup menu
        JPopupMenu menu = new JPopupMenu();
        JMenuItem miPaid = new JMenuItem("✓  Mark as Paid");
        miPaid.setFont(new Font("SansSerif", Font.PLAIN, 13));
        menu.add(miPaid);
        t.setComponentPopupMenu(menu);
        miPaid.addActionListener(e -> markPaid(t, model, type));

        return t;
    }

    private JPanel tableWrap(JTable t) {
        JScrollPane scroll = new JScrollPane(t);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 205, 215)));
        scroll.setPreferredSize(new Dimension(0, 152));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
        wrap.add(scroll);
        return wrap;
    }

    // ─── Style helpers ─────────────────────────────────────────────
    private JPanel statCard(String title, String value, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 215)),
                new EmptyBorder(12, 14, 12, 14))));
        JLabel lT = new JLabel(title);
        lT.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lT.setForeground(new Color(110, 115, 125));
        JLabel lV = new JLabel(value);
        lV.setFont(new Font("SansSerif", Font.BOLD, 26));
        lV.setForeground(accent);
        lV.setName("val");
        p.add(lT, BorderLayout.NORTH);
        p.add(lV, BorderLayout.CENTER);
        return p;
    }

    private JLabel findValLabel(JPanel card) {
        for (Component c : card.getComponents())
            if (c instanceof JLabel && "val".equals(c.getName()))
                return (JLabel) c;
        return new JLabel();
    }

    private JLabel groupLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(110, 115, 125));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

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
            public Component getTableCellRendererComponent(JTable tbl, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, row, col);
                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 255));
                setBorder(new EmptyBorder(0, 5, 0, 5));
                return this;
            }
        });
    }

    private JButton btn(String text, Color bg, int w, int h) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(w, h));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
