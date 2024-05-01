import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.DataInput;
import java.io.DataOutput;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBInputFormat;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndex {

    public static class MyDBWritable implements DBWritable, Writable {
        private long id;
        private String content;

        // Required by DBWritable
        @Override
        public void readFields(ResultSet resultSet) throws SQLException {
            id = resultSet.getLong("id");
            content = resultSet.getString("content");
        }

        @Override
        public void write(PreparedStatement statement) throws SQLException {
            statement.setLong(1, id);
            statement.setString(2, content);
        }

        // Required by Writable
        @Override
        public void readFields(DataInput in) throws IOException {
            id = in.readLong();
            content = in.readUTF();
        }

        @Override
        public void write(DataOutput out) throws IOException {
            out.writeLong(id);
            out.writeUTF(content);
        }

        public long getId() {
            return id;
        }

        public String getContent() {
            return content;
        }
    }

    public static class InvertedIndexMapper extends Mapper<LongWritable, MyDBWritable, Text, Text> {
        private final Text word = new Text();
        private final Text pageId = new Text();

        @Override
        protected void map(LongWritable key, MyDBWritable value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.getContent());
            pageId.set(String.valueOf(value.getId()));

            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, pageId);
            }
        }
    }

    public static class InvertedIndexReducer extends Reducer<Text, Text, Text, Text> {
        private final Text result = new Text();

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            Set<String> pages = new HashSet<>();

            for (Text val : values) {
                pages.add(val.toString());
            }

            StringBuilder fileList = new StringBuilder();
            for (String page : pages) {
                fileList.append(page).append(", ");
            }

            result.set(fileList.toString());
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        DBConfiguration.configureDB(conf,
            "org.postgresql.Driver",
            "jdbc:postgresql://db:5432/postgres",
            "postgres",
            "postgres");

        Job job = Job.getInstance(conf, "Inverted Index from DB");
        job.setJarByClass(InvertedIndex.class);
        job.setMapperClass(InvertedIndexMapper.class);
        job.setReducerClass(InvertedIndexReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(DBInputFormat.class);
        String[] fields = {"id", "content"};
        DBInputFormat.setInput(job, MyDBWritable.class, "djangoapp_page", null, "id", fields);

        FileOutputFormat.setOutputPath(job, new Path(args[0]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
