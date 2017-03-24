package edu.hku.cs.dft

import java.io.FileNotFoundException

import edu.hku.cs.dft.SampleMode.SampleMode
import edu.hku.cs.dft.TrackingMode.TrackingMode
import org.apache.spark.SparkConf

import scala.io.Source


/**
  * Created by max on 8/3/2017.
  */
/**
  * A [[ArgumentHandle]] specify the interface needed to parse the arguments for
  * our data-flow tracking framework
  * */

trait ArgumentHandle {
  def init(): Boolean
  def parseArgs(key: String): String
  def setKeyValue(key: String, value: String): Unit
}

/**
  * Parse configuration from spark
*/


/*
class SparkArgumentHandle(sparkConf: SparkConf) extends ArgumentHandle {
  override def init(): Boolean = true

  override def parseArgs(key: String): String = {
    sparkConf.get("spark.dft." + key)
  }
}
*/

class ConfFileHandle(filename: String) extends ArgumentHandle with DFTEnv.DFTLogging {

  var keyMap: Map[String, String] = Map()

  try {
    logInfo("Read configuration file from " + filename)
    for (line <- Source.fromFile(filename).getLines()) {
      if (!line.trim.startsWith("#")) {
        val arr = line.split("=")
        if (arr.length >= 3) throw new Exception("wrong format")
        val key = arr(0).trim
        val value = arr(1).trim
        keyMap += key -> value
        logInfo("conf: " + key + " -> " + value)
      }
    }
  } catch {
    case e: FileNotFoundException => logInfo("conf file " + filename + " not found")
    // use the default setting
  }

  override def init(): Boolean = true

  override def parseArgs(key: String): String = {
    keyMap.getOrElse(key, null)
  }

  override def setKeyValue(key: String, value: String): Unit = {
    keyMap += key -> value
  }
}

/**
* Parse the configuration from code
* */
class CustomArgumentHandle extends ArgumentHandle {
  override def init(): Boolean = true

  override def parseArgs(key: String): String = {
    key match {
      case DefaultArgument.CONF_HOST => DefaultArgument.host
      case DefaultArgument.CONF_PORT => DefaultArgument.port.toString
      case DefaultArgument.CONF_TRACKING => "mix"
      case DefaultArgument.CONF_SAMPLE => "off"
      case DefaultArgument.CONF_MODE => "server"
      case DefaultArgument.CONF_PHOSPHOR_JAVA => ""
      case DefaultArgument.CONF_PHOSPHOR_JAR => ""
      case DefaultArgument.CONF_PHOSPHOR_CACHE => "./phosphor_cache/"
      //TODO add partition
    }
  }

  override def setKeyValue(key: String, value: String) = {}
}

object DefaultArgument {

  val CONF_PREFIX = "--"

  val CONF_DFT: String = "dft-on"
  val CONF_HOST: String = "dft-host"
  val CONF_PORT: String = "dft-port"
  val CONF_TRACKING: String = "dft-tracking"
  val CONF_SAMPLE: String = "dft-sample"
  val CONF_MODE: String = "dft-mode"
  val CONF_PHOSPHOR_JAVA: String = "dft-phosphor-java"
  val CONF_PHOSPHOR_JAR: String = "dft-phosphor-jar"
  val CONF_PHOSPHOR_CACHE: String = "dft-phosphor-cache"

  val _CONF_DFT: String = CONF_PREFIX + CONF_DFT
  val _CONF_HOST: String = CONF_PREFIX + CONF_HOST
  val _CONF_PORT: String = CONF_PREFIX + CONF_PORT
  val _CONF_TRACKING: String = CONF_PREFIX + CONF_TRACKING
  val _CONF_SAMPLE: String = CONF_PREFIX + CONF_SAMPLE
  val _CONF_MODE: String = CONF_PREFIX + CONF_MODE
  val _CONF_PHOSPHOR_JAVA: String = CONF_PREFIX + CONF_PHOSPHOR_JAVA
  val _CONF_PHOSPHOR_JAR: String = CONF_PREFIX + CONF_PHOSPHOR_JAR
  val _CONF_PHOSPHOR_CACHE: String = CONF_PREFIX + CONF_PHOSPHOR_CACHE


  val host: String = "127.0.0.1"
  val port: Int = 8787
  val trackingMode: TrackingMode = TrackingMode.RuleTracking
  val sampleMode: SampleMode = SampleMode.Off
  val mode: String = "server"
  // By default, 10% of the data is used when in the data sampling mode
  val sampleInt: Int = 10
  val partitionPath: String = "default.scheme"
  val confFile = "/etc/dft.conf"
}