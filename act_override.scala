import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import scala.actors.Actor._
import scala.actors._
import scala.math._
import scala.util.Random
import scala.io.Source._
import scala.collection.mutable._
import java.io._
import scala.actors.!

case class msgFinal(msg: String)
case class msgFinalD(ip: String, seqNo: Int, msg: String)
case class msgAddAddr(newip: String)

abstract class CustomLogger extends Actor {

  override def react(handler: PartialFunction[Any, Unit]): Nothing =
    {
     val qf = new PartialFunction[Any,Unit] {
   	  def isDefinedAt(a: Any) = handler.isDefinedAt(a)
   	  def apply(a: Any): Unit = {
   		 println("yayyy!") 
   	    //log(a)  // Maybe add more logic to know what a is
   		  handler(a)
   	 }
 }
      super.react(handler: PartialFunction[Any, Unit]): Nothing
    }

  override def !(msg: Any): Unit = {
    
    super.!(msg)
    
  }
}

class Daemon(ip: String) extends CustomLogger {

  var addrList = scala.collection.mutable.ListBuffer[String]()

  var nomsgs = 0
  var seqNo = 0

  override def act() {

    RemoteActor.classLoader = getClass().getClassLoader()
    val spString = ip.split(":")
    alive(spString(1).toInt)
    register('daemon, self)

    loop {

      react {
        case msgAddAddr(newip) => {
          addrList += newip
        }

        case msgFinalD(senderIp, sno, mString) => {

           println("Received From : " + senderIp)
         
          //log here
          self ! msgFinal(mString)

        }

        case msgFinal(mstr) => {
          println("Received Final Message")

          val r = new Random()
          //println(addrList.size)
          val x = r.nextInt(addrList.size)
          val spString = addrList(x).split(":")
          val nd = Node(spString(0), spString(1).toInt)
          val tempdaemonactor = select(nd, 'daemon)
          tempdaemonactor ! msgFinal("anskd")

        }

      }
    } //loop  
  } //act

} //class

object actorlog {
  def main(args: Array[String]) {

    if (args.length != 1) {
      println("Argument missing : [number of actors] ")
    } else if (args.length == 1) {
      val actSize = args(0).toInt;
      var d = new Array[String](actSize)

      var ad = new Array[Actor](actSize)

      for (i <- 0 until actSize) {
        d(i) = "127.0.0.1:" + (2500 + i)
      }

      for (i <- 0 until actSize) {
        val actr = new Daemon("127.0.0.1:" + (2500 + i))
        ad(i) = actr
      }

      for (i <- 0 until actSize) {
        ad(i).start
      }

      for (i <- 0 until actSize) {
        val spString = d(i).split(":")
        val nd = Node(spString(0), spString(1).toInt)
        val tempdaemonactor = select(nd, 'daemon)

        for (j <- 0 until actSize) {
          if (i != j) {
            tempdaemonactor ! msgAddAddr("127.0.0.1:" + (2500 + j))
          }
        }
      }

      for (i <- 0 until 1) {
        //actSize) {
        val spString = d(i).split(":")
        val nd = Node(spString(0), spString(1).toInt)
        val tempdaemonactor = select(nd, 'daemon)
        tempdaemonactor ! msgFinal("Ank")

      }
    }
  }
}