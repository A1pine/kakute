# Kakute

Kakute is the first Information Flow Tracking (IFT) system for big-data. It is built
on Spark, a popular big-data processing engine in both industry and academia.

Kakute provides a unified API for adding / removing tags for data and controlling IFT across hosts. We have built several applications based on Kakute for **debugging**, **data provenance**, **preventing information leakage** and **performance optimization**.

Kakute is based on a previous Information Flow Tracking framework **Phosphor** ([Code](https://github.com/Programming-Systems-Lab/Phosphor), [Paper](http://www.jonbell.net/oopsla2014-phosphor-preprint.pdf)). We have fixed some bugs, improved the performance and usability. Our optimized **Phosphor** can be [here](https://github.com/hku-systems/kakute).

This work has been accepted by 33th Annual Computer Security Applications Conference (ACSAC'17), and you can see our design details in [this paper](null).

For those who would like to reproduce result in the paper, you can find the dataset in **data/kakute**.

## Building Kakute
### Building Phosphor
Install dependencies
```
apt-get install openjdk-sdk-8 openjdk-sdk-8-source maven
```
Download phosphor, built it and instrument Java JDK
```
git clone https://github.com/hku-systems/phosphor.git
cd phosphor
mvn clean verify
```

Download Kakute
```
git clone https://github.com/hku-systems/kakute.git

```

Setup the correct phosphor directory in core/pom.xml (change $DIRECTORY_TO_PHOSPHOR to where the PHOSPHOR locates, the default is $HOME)
```
<dependency>
  <groupId>edu.columbia.cs</groupId>
  <artifactId>phosphor</artifactId>
  <version>0.0.3</version>
  <scope>system</scope>
  <systemPath>$DIRECTORY_TO_PHOSPHOR/Phosphor/target/Phosphor-0.0.3-SNAPSHOT.jar</systemPath>
</dependency>
```

Build Kakute, it is the same as building Spark
```
cd kakute
build/mvn -DskipTests clean package
```

Modify **dft.conf** according your configuration of phosphor. (change $DIRECTORY_TO_PHOSPHOR to where the PHOSPHOR locates, the default is $HOME)
```
dft-host = 127.0.0.1 // driver host ip
dft-port = 8787 // driver host port
dft-phosphor-java = $DIRECTORY_TO_PHOSPHOR/Phosphor/target/
dft-phosphor-jar = $DIRECTORY_TO_PHOSPHOR/Phosphor/target/Phosphor-0.0.3-SNAPSHOT.jar
dft-phosphor-cache = $DIRECTORY_FOR_CACHE
graph_dump_path = graph.dump
dft-tracking = rule
dft-input-taint = false
dft-scheme = true
```

Congrats. You have finished building Kakute, you can try with a simple example below.

# Apache Spark

Spark is a fast and general cluster computing system for Big Data. It provides
high-level APIs in Scala, Java, Python, and R, and an optimized engine that
supports general computation graphs for data analysis. It also supports a
rich set of higher-level tools including Spark SQL for SQL and DataFrames,
MLlib for machine learning, GraphX for graph processing,
and Spark Streaming for stream processing.

<http://spark.apache.org/>


## Online Documentation

You can find the latest Spark documentation, including a programming
guide, on the [project web page](http://spark.apache.org/documentation.html).
This README file only contains basic setup instructions.

## Building Spark

Spark is built using [Apache Maven](http://maven.apache.org/).
To build Spark and its example programs, run:

    build/mvn -DskipTests clean package

(You do not need to do this if you downloaded a pre-built package.)

You can build Spark using more than one thread by using the -T option with Maven, see ["Parallel builds in Maven 3"](https://cwiki.apache.org/confluence/display/MAVEN/Parallel+builds+in+Maven+3).
More detailed documentation is available from the project site, at
["Building Spark"](http://spark.apache.org/docs/latest/building-spark.html).

For general development tips, including info on developing Spark using an IDE, see ["Useful Developer Tools"](http://spark.apache.org/developer-tools.html).

## Interactive Scala Shell

The easiest way to start using Spark is through the Scala shell:

    ./bin/spark-shell

Try the following command, which should return 1000:

    scala> sc.parallelize(1 to 1000).count()

## Interactive Python Shell

Alternatively, if you prefer Python, you can use the Python shell:

    ./bin/pyspark

And run the following command, which should also return 1000:

    >>> sc.parallelize(range(1000)).count()

## Example Programs

Spark also comes with several sample programs in the `examples` directory.
To run one of them, use `./bin/run-example <class> [params]`. For example:

    ./bin/run-example SparkPi

will run the Pi example locally.

You can set the MASTER environment variable when running examples to submit
examples to a cluster. This can be a mesos:// or spark:// URL,
"yarn" to run on YARN, and "local" to run
locally with one thread, or "local[N]" to run locally with N threads. You
can also use an abbreviated class name if the class is in the `examples`
package. For instance:

    MASTER=spark://host:7077 ./bin/run-example SparkPi

Many of the example programs print usage help if no params are given.

## Running Tests

Testing first requires [building Spark](#building-spark). Once Spark is built, tests
can be run using:

    ./dev/run-tests

Please see the guidance on how to
[run tests for a module, or individual tests](http://spark.apache.org/developer-tools.html#individual-tests).

## A Note About Hadoop Versions

Spark uses the Hadoop core library to talk to HDFS and other Hadoop-supported
storage systems. Because the protocols have changed in different versions of
Hadoop, you must build Spark against the same version that your cluster runs.

Please refer to the build documentation at
["Specifying the Hadoop Version"](http://spark.apache.org/docs/latest/building-spark.html#specifying-the-hadoop-version)
for detailed guidance on building for a particular distribution of Hadoop, including
building for particular Hive and Hive Thriftserver distributions.

## Configuration

Please refer to the [Configuration Guide](http://spark.apache.org/docs/latest/configuration.html)
in the online documentation for an overview on how to configure Spark.

## Contributing

Please review the [Contribution to Spark guide](http://spark.apache.org/contributing.html)
for information on how to get started contributing to the project.
