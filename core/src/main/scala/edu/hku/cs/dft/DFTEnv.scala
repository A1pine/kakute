package edu.hku.cs.dft

import edu.hku.cs.dft.datamodel.{GraphDumper, GraphManager}
import edu.hku.cs.dft.SampleMode.SampleMode
import edu.hku.cs.dft.TrackingMode.TrackingMode
import edu.hku.cs.dft.network.{EndpointDispatcher, EndpointRegister, NettyClient, NettyServer}
import edu.hku.cs.dft.optimization.RuleLocalControl
import edu.hku.cs.dft.tracker.{PhosphorRunner, TrackingPolicy, TrackingType}
import edu.hku.cs.dft.tracker.TrackingType.TrackingType

/**
  * Created by jianyu on 3/6/17.
  */

object TrackingMode extends Enumeration {
  type TrackingMode = Value
  val RuleTracking, FullTracking, MixTracking = Value
}

object SampleMode extends Enumeration {
  type SampleMode = Value
  val Sample, Machine, SampleAndMachine, Off = Value
}

class DFTEnv(val argumentHandle: ArgumentHandle) {
  argumentHandle.init()
  val serverPort: Int = argumentHandle.parseArgs("port") match {
    case s: String => s.toInt
    case _ => DefaultArgument.port
  }
  val serverHost: String = argumentHandle.parseArgs("host") match {
    case s: String => s
    case _ => DefaultArgument.host
  }
  val trackingMode: TrackingMode = {
    argumentHandle.parseArgs("tracking") match {
      case "full" => TrackingMode.FullTracking
      case "rule" => TrackingMode.RuleTracking
      case "mix" => TrackingMode.MixTracking
      case _ => DefaultArgument.trackingMode
    }
  }

  val trackingType: TrackingType = {
    argumentHandle.parseArgs("type") match {
      case "key" => TrackingType.Keys
      case "value" => TrackingType.Values
      case "KeyValue" => TrackingType.KeyValues
      case _ => TrackingType.KeyValues
    }
  }

  val sampleMode: SampleMode = {
    argumentHandle.parseArgs("sample") match {
      case "sample" => SampleMode.Sample
      case "machine" => SampleMode.Machine
      case "sample&machine" => SampleMode.SampleAndMachine
      case "off" => SampleMode.Off
      case _ => DefaultArgument.sampleMode
    }
  }

  val auto_taint_input: Boolean = {
    argumentHandle.parseArgs("input_taint") match {
      case "true" => true
      case _ => false
    }
  }

  //use percentage sampling
  val sampleNum: Int = {
    val stringInt = argumentHandle.parseArgs("sample_int")
    if (stringInt != null) {
      Integer.valueOf(stringInt)
    } else {
      DefaultArgument.sampleInt
    }
  }

  var isServer: Boolean = argumentHandle.parseArgs("mode") match {
    case "server" => true
    case "worker" => false
    case _ => false
  }

  val partitionSchemeOutput: String = {
    argumentHandle.parseArgs("partition_output") match {
      case s: String => s
      case _ => DefaultArgument.partitionPath
    }
  }

  val phosphorEnv: PhosphorEnv = {
    var java = argumentHandle.parseArgs("phosphor_java")
    if (java == null) {
      java = "phosphor/bin/"
    }
    var jar = argumentHandle.parseArgs("phosphor_jar")
    if (jar == null) {
      jar = "phosphor/phosphor.jar"
    }
    var cache = argumentHandle.parseArgs("phosphor_cache")
    if (cache == null) {
      cache = "phosphor/cache"
    }
    PhosphorEnv(java, jar, cache)
  }
  val graphDumpPath: String = argumentHandle.parseArgs("graph_dump_path") match {
    case s: String => s
    case _ => "phosphor/graph.dump"
  }
}

case class PhosphorEnv(phosphorJava: String, phosphorJar: String, cache: String)

object DFTEnv {

  type DFTLogging = org.apache.spark.internal.Logging

  private var _confPath: String = DefaultArgument.confFile

  private var _dftEnv: DFTEnv = _

  var trackingPolicy: TrackingPolicy = _

  var graphManager: GraphManager = _

  var localControl: RuleLocalControl = _

  var phosphorRunner: PhosphorRunner = _

  var networkEnv: EndpointRegister = _

  // set the conf path
  def pathInit(path: String): Unit = _confPath = path

  // init the common variables and objects
  def commonInit(): Unit = {
    _dftEnv = new DFTEnv(new ConfFileHandle(_confPath))

  }

  def dftEnv(): DFTEnv = {
    if (_dftEnv == null) throw new Exception("DFT Environment not set")
    _dftEnv
  }

  def init(any: Any): Unit = {
    if (_dftEnv.isServer) {
      networkEnv = new NettyServer(new EndpointDispatcher, _dftEnv)
    } else {
      networkEnv = new NettyClient(new EndpointDispatcher, _dftEnv)
    }
  }

  // TODO: we may need to check if the tracking engine could be working
  // TODO: if that is not working, we will reject every tracking requests
  def worker_init(): Unit = {
    trackingPolicy = new TrackingPolicy(DFTEnv.dftEnv().trackingType,
      DFTEnv.dftEnv().trackingMode,
      DFTEnv.dftEnv().auto_taint_input)

    phosphorRunner = new PhosphorRunner(DFTEnv.dftEnv().phosphorEnv.cache,
      DFTEnv.dftEnv().phosphorEnv.phosphorJar,
      DFTEnv.dftEnv().phosphorEnv.phosphorJava)
  }

  def server_init(any: Any): Unit = {
    networkEnv = new NettyServer(new EndpointDispatcher, _dftEnv)
    new Thread(new Runnable {
      override def run():Unit = networkEnv.run()
    }).start()

    // sleep 1s to make sure the network is on
    Thread.sleep(1000)
    _dftEnv.isServer = true
    graphManager = new GraphManager
    networkEnv.register(graphManager)
  }

  def client_init(any: Any): Unit = {
    networkEnv = new NettyClient(new EndpointDispatcher, _dftEnv)
    new Thread(new Runnable {
      override def run():Unit = networkEnv.run()
    }).start()
    Thread.sleep(1000)
    // sleep 1s to make sure the network is on
    _dftEnv.isServer = false
    localControl = new RuleLocalControl
    networkEnv.register(localControl)
  }

  def stop_all(): Unit = {
    networkEnv.stop()
    if (DFTEnv.dftEnv().isServer) {
      val graphDumper = new GraphDumper(DFTEnv.dftEnv().graphDumpPath)
      graphDumper.open()
      graphDumper.dumpGraph(graphManager)
      graphDumper.close()
    }
  }
}
