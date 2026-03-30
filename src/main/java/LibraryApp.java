import javax.swing.SwingUtilities;

public class LibraryApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new loginform().setVisible(true);
        });
    }
}
