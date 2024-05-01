#!/bin/bash

# test the hadoop cluster by running wordcount

# # create input files
# mkdir input

# # create input directory on HDFS
# hadoop fs -mkdir -p input

# # put input files to HDFS
# hdfs dfs -put ./input/* input

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:/path/to/postgresql-jdbc.jar
export CLASSPATH=$CLASSPATH:$(hadoop classpath)

javac InvertedIndex.java
jar cf InvertedIndex.jar *.class

# run inverted index
hadoop jar /root/InvertedIndex.jar InvertedIndex input output