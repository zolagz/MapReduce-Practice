package com.huike.action03;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MaxNum extends Configured implements Tool {

	public static class MaxNumMapper extends Mapper<LongWritable, Text, Text, Text> {

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String[] parameters = line.split("\\s+");
			String urlId = parameters[0];
			context.write(new Text(urlId), new Text(String.valueOf(parameters[1])));
		}
	}

	public static class MaxNumReducer extends Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			long max = 0L;
			for (Text text : values) {
				if (Long.parseLong(text.toString()) > max) {
					max = Long.parseLong(text.toString());
				}
			}
			context.write(key, new Text(String.valueOf(max)));
		}
	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		Path mypath = new Path(args[1]);
		FileSystem hdfs = mypath.getFileSystem(conf);
		if (hdfs.isDirectory(mypath)) {
			hdfs.delete(mypath, true);
		}
		Job job = Job.getInstance(conf, "MaxNum");
		job.setNumReduceTasks(1);
		job.setMapperClass(MaxNumMapper.class);
		job.setCombinerClass(MaxNumReducer.class);
		job.setReducerClass(MaxNumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setJarByClass(MaxNum.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		String[] args0 = { "/test/action03/MaxNum.txt", "/test/action03/output/" };
		int res = ToolRunner.run(new Configuration(), new MaxNum(), args0);
		System.out.println(res);

	}
}
