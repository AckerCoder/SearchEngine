FROM ubuntu:20.04
WORKDIR /root

# install openssh-server, openjdk and wget
RUN apt-get update && apt-get install -y openssh-server openjdk-8-jdk wget
RUN apt-get install -y curl

RUN ls -l /usr/lib/jvm/

# install hadoop 2.7.2
RUN wget https://github.com/kiwenlau/compile-hadoop/releases/download/2.7.2/hadoop-2.7.2.tar.gz && \
    tar -xzvf hadoop-2.7.2.tar.gz && \
    mv hadoop-2.7.2 /usr/local/hadoop && \
    rm hadoop-2.7.2.tar.gz

RUN curl -o /usr/local/lib/postgresql-jdbc.jar https://jdbc.postgresql.org/download/postgresql-42.2.5.jre7.jar

# set environment variable
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-arn64
ENV HADOOP_HOME=/usr/local/hadoop
ENV PATH=$PATH:/usr/local/hadoop/bin:/usr/local/hadoop/sbin
ENV CLASSPATH $CLASSPATH:/usr/local/lib/postgresql-jdbc.jar

# ssh without key
RUN ssh-keygen -t rsa -f ~/.ssh/id_rsa -P '' && \
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

RUN mkdir -p ~/hdfs/namenode && \
    mkdir -p ~/hdfs/datanode && \
    mkdir $HADOOP_HOME/logs

COPY config/* /tmp/

RUN mv /tmp/ssh_config ~/.ssh/config && \
    mv /tmp/hadoop-env.sh /usr/local/hadoop/etc/hadoop/hadoop-env.sh && \
    mv /tmp/hdfs-site.xml $HADOOP_HOME/etc/hadoop/hdfs-site.xml && \
    mv /tmp/core-site.xml $HADOOP_HOME/etc/hadoop/core-site.xml && \
    mv /tmp/mapred-site.xml $HADOOP_HOME/etc/hadoop/mapred-site.xml && \
    mv /tmp/yarn-site.xml $HADOOP_HOME/etc/hadoop/yarn-site.xml && \
    mv /tmp/slaves $HADOOP_HOME/etc/hadoop/slaves && \
    mv /tmp/start-hadoop.sh ~/start-hadoop.sh && \
    mv /tmp/run-wordcount.sh ~/run-wordcount.sh && \
    mv /tmp/run-inverted-index.sh ~/run-inverted-index.sh && \
    mv /tmp/run-indexation.sh ~/run-indexation.sh && \
    mv /tmp/InvertedIndex.java ~/InvertedIndex.java && \
    mv /tmp/WordCount.java ~/WordCount.java && \
    mv /tmp/ExportDataToTextFile.java ~/ExportDataToTextFile.java && \
    mv /tmp/ExportToHDFS.java ~/ExportToHDFS.java && \
    mv /tmp/PageRank.java ~/PageRank.java

RUN chmod +x ~/start-hadoop.sh && \
    chmod +x ~/run-wordcount.sh && \
    chmod +x ~/run-inverted-index.sh && \
    chmod +x ~/run-indexation.sh && \
    chmod +x $HADOOP_HOME/sbin/start-dfs.sh && \
    chmod +x $HADOOP_HOME/sbin/start-yarn.sh

# format namenode
RUN /usr/local/hadoop/bin/hdfs namenode -format

CMD [ "sh", "-c", "service ssh start; bash"]
