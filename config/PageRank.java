import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class PageRank {

    public static class PageRankMapper extends Mapper<Object, Text, Text, Text> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] parts = value.toString().split("\\t");
            String pageId = parts[0];
            double currentRank = Double.parseDouble(parts[1]);
            if (parts.length > 2) {
                String[] links = parts[2].split(",");
                for (String link : links) {
                    context.write(new Text(link), new Text(String.valueOf(currentRank / links.length)));
                }
                // Pass along the link structure
                context.write(new Text(pageId), new Text("|" + parts[2]));
            }
        }
    }

    public static class PageRankReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            double sum = 0.0;
            String links = "";
            for (Text value : values) {
                String content = value.toString();
                if (content.startsWith("|")) {
                    links = content.substring(1);
                } else {
                    sum += Double.parseDouble(content);
                }
            }
            double newRank = 0.85 * sum + 0.15;
            context.write(key, new Text(newRank + "\t" + links));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "PageRank");
        job.setJarByClass(PageRank.class);
        job.setMapperClass(PageRankMapper.class);
        job.setReducerClass(PageRankReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
