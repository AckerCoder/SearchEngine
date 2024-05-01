import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExportDataToTextFile {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://db:5432/postgres";
        String user = "postgres";
        String password = "postgres";
        String filePath = "data.txt";

        // SQL query to execute
        String query = "SELECT * FROM djangoapp_page";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             FileWriter fileWriter = new FileWriter(filePath);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            // Fetch each row from the result set
            while (rs.next()) {
                // Assuming two columns: id (int) and name (String)
                int id = rs.getInt("id");
                String name = rs.getString("content");

                // Write the data to file
                printWriter.println(id + ", " + name);
            }
            System.out.println("Data exported successfully to " + filePath);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
