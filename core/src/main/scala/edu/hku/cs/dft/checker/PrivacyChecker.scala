package edu.hku.cs.dft.checker

import edu.hku.cs.dft.network.Message
import edu.hku.cs.dft.tracker.TrackingTaint.TrackingTaint
import edu.hku.cs.dft.tracker._

/**
  * Created by jianyu on 9/9/17.
  */
class PrivacyChecker extends IFTChecker {
  override val taint: TrackingTaint = TrackingTaint.IntTaint
  override val tapConf: TapConf = null
  override val localChecker: LocalChecker = new LocalChecker {

    override def instrument(): Unit = {
//      addMethod()
    }

    override def receiveAndReply(message: Message): Message = {
      null
    }

    override def onRegister(): Unit = {}

    override val id: String = "privacy"
  }
  override val globalChecker: GlobalChecker = new GlobalChecker {

    override def stop(): Unit = {}

    override def receiveAndReply(message: Message): Message = {
      null
    }

    override def onRegister(): Unit = {}

    override val id: String = "privacy"
  }
  override val across_machine: Boolean = true
}
