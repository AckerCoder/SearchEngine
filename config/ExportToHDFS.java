import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

public class ExportToHDFS {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://db:5432/postgres";
        String user = "postgres";
        String password = "postgres";
        String query = "SELECT id, content FROM djangoapp_page";  // Replace "your_table_name" with actual table name

        Configuration conf = new Configuration();
        conf.addResource(new Path("$HADOOP_HOME/etc/hadoop/core-site.xml"));
        conf.addResource(new Path("$HADOOP_HOME/etc/hadoop/hdfs-site.xml"));

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            FileSystem fs = FileSystem.get(conf);

            while (rs.next()) {
                String id = rs.getString("id");
                String content = rs.getString("content");
                Path file = new Path("pages/" + id + ".txt");

                try (org.apache.hadoop.fs.FSDataOutputStream os = fs.create(file, true)) {
                    os.writeUTF(content);
                } catch (Exception e) {
                    System.out.println("Error writing to HDFS: " + e.getMessage());
                }
            }
            System.out.println("Data exported successfully to HDFS.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
