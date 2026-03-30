import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseHelper.java
 * ─────────────────────────────────────────────────────────────────
 * Single class that manages the MySQL connection for the whole project.
 * Every panel calls DatabaseHelper.getConnection() to talk to the DB.
 *
 * ► ONLY CHANGE THE 3 LINES MARKED BELOW ◄
 */
public class DatabaseHelper {

    // ╔══════════════════════════════════════════════════════════╗
    // ║  CHANGE THESE 3 VALUES TO MATCH YOUR MySQL SETUP        ║
    // ╚══════════════════════════════════════════════════════════╝

    // Your database name (the one you created in MySQL Workbench / phpMyAdmin)
    private static final String DB_NAME  = "library";

    // Your MySQL username (usually "root" for local installs)
    private static final String USERNAME = "root";

    // Your MySQL password
    private static final String PASSWORD = "root";

    // ─── Do NOT change anything below this line ───────────────
    private static final String URL =
        "jdbc:mysql://localhost:3306/" + DB_NAME;

    static {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("═══════════════════════════════════════════════════");
            System.err.println("  ERROR: MySQL JDBC Driver JAR not found!");
            System.err.println("  Did you add mysql-connector-j-X.X.X.jar to");
            System.err.println("  your NetBeans project Libraries?");
            System.err.println("  (Right-click project → Properties → Libraries)");
            System.err.println("═══════════════════════════════════════════════════");
        }
    }

    /**
     * Returns an open Connection to your MySQL database.
     * Always close it when done:
     *   try (Connection conn = DatabaseHelper.getConnection()) { ... }
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Run this main() once to verify your connection works.
     * Right-click DatabaseHelper.java → Run File
     */
    public static void main(String[] args) {
        System.out.println("Testing MySQL connection...");
        System.out.println("URL: " + URL);
        System.out.println("User: " + USERNAME);

        try (Connection conn = getConnection()) {
            System.out.println("─────────────────────────────────────────");
            System.out.println("  SUCCESS! Connected to MySQL.");
            System.out.println("  Database: " + conn.getCatalog());
            System.out.println("  MySQL version: " +
                conn.getMetaData().getDatabaseProductVersion());
            System.out.println("─────────────────────────────────────────");
        } catch (SQLException e) {
            System.err.println("─────────────────────────────────────────");
            System.err.println("  FAILED: " + e.getMessage());
            System.err.println("");
            System.err.println("  Common causes:");
            System.err.println("  1. Wrong password in DatabaseHelper.java");
            System.err.println("  2. MySQL server not running");
            System.err.println("     → Open Services panel in NetBeans");
            System.err.println("       and start your MySQL server");
            System.err.println("  3. Database '" + DB_NAME + "' does not exist");
            System.err.println("     → Run the SQL script first");
            System.err.println("─────────────────────────────────────────");
        }
    }
}
