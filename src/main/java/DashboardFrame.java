import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * DashboardFrame
 * – Shows book stats, issue counts, and fine summary cards at the top.
 * – Shows a recent-activity table at the bottom.
 * – Call getContentPanel() to embed inside MainFrame's CardLayout.
 *
 * TODO connections:
 *   loadStats()  → SELECT COUNT / SUM queries on your tables
 *   loadRecent() → JOIN query on student_issue UNION faculty_issue
 */
public class DashboardFrame extends JFrame {

    // Stat-card value labels (kept as fields so loadStats() can update them)
    private JLabel valTotalBooks, valIssuedStu, valIssuedFac, valAvailable;
    private JLabel valTotalFine,  valStuFine,   valFacFine;

    private DefaultTableModel recentModel;

    // Colours
    private static final Color BG       = new Color(238, 242, 250);
    private static final Color C_BLUE   = new Color(25, 75, 140);
    private static final Color C_AMBER  = new Color(180, 110, 0);
    private static final Color C_PURPLE = new Color(80,  60, 160);
    private static final Color C_GREEN  = new Color(30,  120, 60);
    private static final Color C_RED    = new Color(185, 35,  35);

    public DashboardFrame() {
        setTitle("Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /** Returns the panel to embed in MainFrame. */
    public JPanel getContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Page title
        JLabel lblTitle = new JLabel("Dashboard");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(C_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 14, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        // Scrollable body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // ── Row 1: Book stats ──────────────────────────────────────
        body.add(groupLabel("Book Statistics"));
        body.add(Box.createVerticalStrut(6));

        JPanel bookRow = new JPanel(new GridLayout(1, 4, 12, 0));
        bookRow.setOpaque(false);
        bookRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));

        JPanel c1 = statCard("Total Books",            "—", C_BLUE);
        JPanel c2 = statCard("Issued – Student",       "—", C_AMBER);
        JPanel c3 = statCard("Issued – Faculty",       "—", C_PURPLE);
        JPanel c4 = statCard("Available Now",          "—", C_GREEN);

        valTotalBooks = getValLabel(c1);
        valIssuedStu  = getValLabel(c2);
        valIssuedFac  = getValLabel(c3);
        valAvailable  = getValLabel(c4);

        bookRow.add(c1); bookRow.add(c2); bookRow.add(c3); bookRow.add(c4);
        body.add(bookRow);
        body.add(Box.createVerticalStrut(16));

        // ── Row 2: Fine stats ──────────────────────────────────────
        body.add(groupLabel("Fine Summary"));
        body.add(Box.createVerticalStrut(6));

        JPanel fineRow = new JPanel(new GridLayout(1, 3, 12, 0));
        fineRow.setOpaque(false);
        fineRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));

        JPanel f1 = statCard("Total Outstanding Fine", "—", C_RED);
        JPanel f2 = statCard("Student Fine Pending",   "—", C_AMBER);
        JPanel f3 = statCard("Faculty Fine Pending",   "—", C_PURPLE);

        valTotalFine = getValLabel(f1);
        valStuFine   = getValLabel(f2);
        valFacFine   = getValLabel(f3);

        fineRow.add(f1); fineRow.add(f2); fineRow.add(f3);
        body.add(fineRow);
        body.add(Box.createVerticalStrut(16));

        // ── Recent activity table ──────────────────────────────────
        body.add(groupLabel("Recent Activity (Last 10 Transactions)"));
        body.add(Box.createVerticalStrut(6));

        String[] cols = {"Issue ID","Member","Type","Book Title","Issue Date","Due Date","Status"};
        recentModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblRecent = new JTable(recentModel);
        styleTable(tblRecent);
        tblRecent.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());
        int[] w = {70, 145, 70, 220, 90, 90, 75};
        for (int i = 0; i < w.length; i++)
            tblRecent.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        JScrollPane scroll = new JScrollPane(tblRecent);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 205, 215)));
        scroll.setPreferredSize(new Dimension(0, 180));

        JPanel tblWrap = new JPanel(new BorderLayout());
        tblWrap.setOpaque(false);
        tblWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 195));
        tblWrap.add(scroll);
        body.add(tblWrap);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(12);
        root.add(bodyScroll, BorderLayout.CENTER);

        // Load data (replace stubs with DB calls)
        loadStats();
        loadRecent();

        return root;
    }

    // ─────────────────────────────────────────────────────────────
    // TODO: Replace with actual DB queries
    // SELECT COUNT(*) FROM books                           → valTotalBooks
    // SELECT COUNT(*) FROM student_issue WHERE return_date IS NULL → valIssuedStu
    // SELECT COUNT(*) FROM faculty_issue  WHERE return_date IS NULL → valIssuedFac
    // SELECT SUM(available_copies) FROM books              → valAvailable
    // SELECT SUM(fine_amount) FROM student_fine WHERE paid=0 → valStuFine
    // SELECT SUM(fine_amount) FROM faculty_fine  WHERE paid=0 → valFacFine
    // ─────────────────────────────────────────────────────────────
    private void loadStats() {
        valTotalBooks.setText("1,240");
        valIssuedStu.setText("47");
        valIssuedFac.setText("18");
        valAvailable.setText("1,175");
        valTotalFine.setText("Rs.1,450");
        valStuFine.setText("Rs.870");
        valFacFine.setText("Rs.580");
    }

    // ─────────────────────────────────────────────────────────────
    // TODO: Replace with DB query:
    //   SELECT 'Student' type, issue_id, student_id name, book_id, issue_date, due_date
    //   FROM student_issue WHERE return_date IS NULL
    //   UNION ALL
    //   SELECT 'Faculty', issue_id, faculty_id, book_id, issue_date, due_date
    //   FROM faculty_issue WHERE return_date IS NULL
    //   ORDER BY issue_date DESC LIMIT 10
    // ─────────────────────────────────────────────────────────────
    private void loadRecent() {
        recentModel.setRowCount(0);
        Object[][] rows = {
            {"IS0312","Rahul Sharma",   "Student","Introduction to Java",    "01/03/2026","15/03/2026","Issued"},
            {"IF0311","Dr. A. Gupta",   "Faculty","Database Mgmt Systems",   "28/02/2026","14/03/2026","Issued"},
            {"IS0310","Priya Singh",    "Student","Operating Systems",        "14/02/2026","28/02/2026","Overdue"},
            {"IF0309","Prof. R. Kumar", "Faculty","Artificial Intelligence", "20/01/2026","03/02/2026","Returned"},
            {"IS0308","Amit Patel",     "Student","Computer Networks",        "15/01/2026","29/01/2026","Returned"},
        };
        for (Object[] r : rows) recentModel.addRow(r);
    }

    // ─── UI helpers ────────────────────────────────────────────────
    private JPanel statCard(String title, String value, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 215)),
                new EmptyBorder(12, 14, 12, 14))));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lTitle.setForeground(new Color(110, 115, 125));

        JLabel lVal = new JLabel(value);
        lVal.setFont(new Font("SansSerif", Font.BOLD, 26));
        lVal.setForeground(accent);
        lVal.setName("val");

        p.add(lTitle, BorderLayout.NORTH);
        p.add(lVal,   BorderLayout.CENTER);
        return p;
    }

    private JLabel getValLabel(JPanel card) {
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
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("SansSerif", Font.BOLD, 13));
        h.setBackground(new Color(25, 75, 140));
        h.setForeground(Color.WHITE);
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String s = v != null ? v.toString() : "";
                if (!sel) {
                    setBackground(s.equals("Overdue")  ? new Color(255, 228, 228) :
                                  s.equals("Returned") ? new Color(225, 248, 230) :
                                  Color.WHITE);
                }
                setForeground(s.equals("Overdue")  ? C_RED   :
                              s.equals("Returned") ? C_GREEN : C_BLUE);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                return this;
            }
        };
    }
}
