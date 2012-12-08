/*import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import scala.actors.Actor._
import scala.actors._
import scala.math._
import scala.util.Random
import scala.io.Source._
import scala.collection.mutable._
import java.io._

case class msgFinal(msg: String)
case class msgFinalD(ip: String, seqNo: Int, msg: String)
case class msgAddAddr(newip: String)
case class msgDie()
case class msgOver()


abstract class CustomLogger extends Actor {

  var seq = 0

  override def react(handler: PartialFunction[Any, Unit]): Nothing =
    {
      /*case msgFinalD(msg,i,m) => {
        			super.! (msgFinal("Dharkar"))
        	}
    */

      println()
      super.react(handler: PartialFunction[Any, Unit]): Nothing
    }

  override def !(msg: Any): Unit = {

    println("Logging : ")

    msg match {
      case msgFinal(msg: String) => {
        println("Logging : ")

        super.!(msgFinalD("Ankush", 2, "Dharkar"))
      }

    }

    //  println("\t LOG : Sending to ") // sender)
    //  super.!(msg)
  }

}


class Daemon(ip: String) extends Actor {

  var addrList = scala.collection.mutable.ListBuffer[String]()

  var nomsgs = 0
  var seqNo = 0

 
  
  override def !(msg: Any): Unit =
    {

      println("Logging...")
      //super.!(msg)

       
    msg match {
      case msgFinal(msg: String) => {
        println("Logging : ")
        
        super.!(msgFinalD("Ankush", 2, "Dharkar"))
      }

    }
    }
  
  
  
  def act() {
    println("Actor at IP   " + ip + "   started succesfully")

    val bw1 = new BufferedWriter(new FileWriter(ip + ".sclog", true))
    bw1.write(ip + " Started\n");
    bw1.close()

    RemoteActor.classLoader = getClass().getClassLoader()

    val spString = ip.split(":")
    alive(spString(1).toInt)
    register('daemon, self)

    loop {

      react {
        case msgFinal(msg) => {
          println("Received = " + msg)

        }
        case msgAddAddr(newip) => {
          addrList += newip
        }

        case msgOver() => {
          val bw2 = new BufferedWriter(new FileWriter(ip + ".sclog", true))
          bw2.write(ip + " Ended")
          bw2.close()
        }

        case msgDie() => {
          println(ip + " killed prematurely")
          exit()
        }

        case msg => {
          println("Received Default msg")
          
          val r = new Random()
          val x = r.nextInt(addrList.size)

          println("sending to : "+ addrList(x))
          val spString = addrList(x).split(":")
          val nd = Node(spString(0), spString(1).toInt)
          val tempdaemonactor = select(nd, 'daemon)
          tempdaemonactor ! "sending.."

        }

      }

    } //loop  
  } //act

} //class

object actorlog {
  def main(args: Array[String]) {

    if (args.length != 1) { println("Argument missing : [number of actors] ") }
    else if (args.length == 1) {
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

      for (i <- 0 until actSize) {
        val spString = d(i).split(":")
        val nd = Node(spString(0), spString(1).toInt)
        val tempdaemonactor = select(nd, 'daemon)
        tempdaemonactor ! msgFinal("Starting")
        tempdaemonactor ! "Starting"
      
      }

          receiveWithin(300) {
      case TIMEOUT => {
        println("   WATCHOUT !  ")
        Thread.sleep(100)
      }
    }
    }
  }

}

object killActor {
  def main(args: Array[String]) {

    val spString = args(0).split(":")
    val nd = Node(spString(0), spString(1).toInt)
    val tempdaemonactor = select(nd, 'daemon)
    tempdaemonactor ! msgDie()

  }
}*/