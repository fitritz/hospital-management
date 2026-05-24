import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbConnect {
  public static void main(String[] args) {
    String url = (args.length > 0 && !args[0].isEmpty()) ? args[0] : System.getenv("DB_URL");
    String user = (args.length > 1 && !args[1].isEmpty()) ? args[1] : System.getenv("DB_USER");
    String pass = (args.length > 2 && !args[2].isEmpty()) ? args[2] : System.getenv("DB_PASS");
    if (url == null || user == null) {
      System.err.println("Usage: java DbConnect <jdbc-url> <user> <pass>");
      System.err.println("Or set environment variables: DB_URL, DB_USER, DB_PASS");
      System.exit(2);
    }
    try (Connection conn = DriverManager.getConnection(url, user, pass)) {
      System.out.println("Connected to: " + url);
      try (Statement st = conn.createStatement()) {
        ResultSet rs = st.executeQuery("SELECT VERSION()");
        if (rs.next())
          System.out.println("Server version: " + rs.getString(1));
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
