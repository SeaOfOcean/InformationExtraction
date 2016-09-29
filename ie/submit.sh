TargetJar=target/ie-project-1.0-SNAPSHOT-jar-with-dependencies.jar
MainClass=com.intel.ie.SparkBatchDriver


$SPARK_HOME/bin/spark-submit \
  --driver-memory 10g \
  --num-executors 4 \
  --class $MainClass \
  --jars lib/protobuf-java-2.6.1.jar,lib/stanford-corenlp.jar,lib/stanford-english-corenlp-models-current.jar \
  --files config.properties \
  $TargetJar 8


