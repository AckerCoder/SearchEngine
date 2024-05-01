
echo -e "\n"

$HADOOP_HOME/sbin/start-dfs.sh

echo -e "\n"

$HADOOP_HOME/sbin/start-yarn.sh

echo -e "\n"



export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:/path/to/postgresql-jdbc.jar
export CLASSPATH=$CLASSPATH:$(hadoop classpath)

javac ExportToHDFS.java
java ExportToHDFS

javac InvertedIndex.java
jar cvf InvertedIndex.jar *.class
hadoop jar /root/InvertedIndex.jar InvertedIndex pages output-inverted-index
hdfs dfs -cat output/part-r-00000
