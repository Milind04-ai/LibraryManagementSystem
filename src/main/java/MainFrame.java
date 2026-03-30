import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private final String adminName;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeBtn = null;

    // ─── Sidebar colours ───────────────────────────────────────────
    private static final Color SIDEBAR_BG     = new Color(28, 38, 58);
    private static final Color SIDEBAR_HOVER  = new Color(45, 58, 85);
    private static final Color SIDEBAR_ACTIVE = new Color(25, 75, 140);
    private static final Color SIDEBAR_FG     = new Color(195, 210, 235);
    private static final Color SIDEBAR_GRP    = new Color(110, 130, 165);
    private static final Color TOPBAR_BG      = new Color(18, 28, 50);

    // ─── Card keys ─────────────────────────────────────────────────
    public static final String CARD_DASHBOARD  = "DASHBOARD";
    public static final String CARD_BOOKS      = "BOOKS";
    public static final String CARD_ISS_STU    = "ISSUE_STUDENT";
    public static final String CARD_ISS_FAC    = "ISSUE_FACULTY";
    public static final String CARD_RET_STU    = "RETURN_STUDENT";
    public static final String CARD_RET_FAC    = "RETURN_FACULTY";
    public static final String CARD_RECORDS    = "RECORDS";
    public static final String CARD_FINES      = "FINES";

    public MainFrame(String adminName) {
        this.adminName = adminName;
        setTitle("Library Management System – Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(960, 600));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ── Top bar ─────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(TOPBAR_BG);
        topBar.setPreferredSize(new Dimension(0, 46));
        topBar.setBorder(new EmptyBorder(0, 18, 0, 18));

        JLabel lblAppName = new JLabel("Library Management System");
        lblAppName.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAppName.setForeground(Color.WHITE);
        topBar.add(lblAppName, BorderLayout.WEST);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightTop.setOpaque(false);

        JLabel lblAdmin = new JLabel("Admin: " + adminName);
        lblAdmin.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblAdmin.setForeground(new Color(160, 185, 225));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(180, 35, 35));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setPreferredSize(new Dimension(80, 28));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());

        rightTop.add(lblAdmin);
        rightTop.add(btnLogout);
        topBar.add(rightTop, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Sidebar ─────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(new EmptyBorder(8, 0, 8, 0));

        addGroup(sidebar, "OVERVIEW");
        JButton btnDash = addNavBtn(sidebar, "  Dashboard",       CARD_DASHBOARD);

        addGroup(sidebar, "BOOKS");
        addNavBtn(sidebar, "  Book Catalog",   CARD_BOOKS);

        addGroup(sidebar, "ISSUE BOOK");
        addNavBtn(sidebar, "  Issue – Student",  CARD_ISS_STU);
        addNavBtn(sidebar, "  Issue – Faculty",  CARD_ISS_FAC);

        addGroup(sidebar, "RETURN BOOK");
        addNavBtn(sidebar, "  Return – Student", CARD_RET_STU);
        addNavBtn(sidebar, "  Return – Faculty", CARD_RET_FAC);

        addGroup(sidebar, "RECORDS");
        addNavBtn(sidebar, "  All Issued Books", CARD_RECORDS);
        addNavBtn(sidebar, "  Fine Summary",     CARD_FINES);

        sidebar.add(Box.createVerticalGlue());
        add(sidebar, BorderLayout.WEST);

        // ── Content area ────────────────────────────────────────────
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(238, 242, 250));

contentPanel.add(new DashboardFrame().getContentPanel(), CARD_DASHBOARD);
contentPanel.add(new BooksPanel(),                  CARD_BOOKS);
contentPanel.add(new IssuePanel("Student"),         CARD_ISS_STU);
contentPanel.add(new IssuePanel("Faculty"),         CARD_ISS_FAC);
contentPanel.add(new ReturnPanel("Student"),        CARD_RET_STU);
contentPanel.add(new ReturnPanel("Faculty"),        CARD_RET_FAC);
contentPanel.add(new IssuedRecordsFrame().getContentPanel(), CARD_RECORDS);
contentPanel.add(new FinePanel(),                   CARD_FINES);

        add(contentPanel, BorderLayout.CENTER);

        // Activate Dashboard button
        activateBtn(btnDash);
        cardLayout.show(contentPanel, CARD_DASHBOARD);
    }

    // ── Sidebar helpers ─────────────────────────────────────────────
    private void addGroup(JPanel sidebar, String title) {
        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(SIDEBAR_GRP);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(200, 28));
        lbl.setBorder(new EmptyBorder(9, 0, 1, 0));
        sidebar.add(lbl);
    }

    private JButton addNavBtn(JPanel sidebar, String label, String cardKey) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(200, 36));
        btn.setPreferredSize(new Dimension(200, 36));
        btn.setMinimumSize(new Dimension(200, 36));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(SIDEBAR_FG);
        btn.setBackground(SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, cardKey);
            activateBtn(btn);
        });

        sidebar.add(btn);
        return btn;
    }

    private void activateBtn(JButton btn) {
        if (activeBtn != null) {
            activeBtn.setBackground(SIDEBAR_BG);
            activeBtn.setForeground(SIDEBAR_FG);
        }
        activeBtn = btn;
        btn.setBackground(SIDEBAR_ACTIVE);
        btn.setForeground(Color.WHITE);
    }

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this,
            "Logout and return to login screen?", "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            new loginform().setVisible(true);
            dispose();
        }
    }
}
