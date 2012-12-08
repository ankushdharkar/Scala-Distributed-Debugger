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

case class msgFinal(ip: String, seqNo: Int, msg: String)
case class msgAddAddr(newip: String)
case class msgDie()
case class msgOver()

class Daemon(ip: String) extends Actor {

   
override def react(handler: PartialFunction[Any, Unit]): Nothing =
{

  println()
  super.react(handler: PartialFunction[Any, Unit]): Nothing
} 
  
  
  override def !(msg:Any):Unit =
{
    println("\t LOG : Sending to ") // sender)
    super.!(msg)
}


  var addrList = scala.collection.mutable.ListBuffer[String]()

  var nomsgs = 0
  var seqNo = 0

  def sendMsg(sendIp: String, seqNo: Int, msg: String) = {
    val bw = new BufferedWriter(new FileWriter(ip + ".sclog", true))
    bw.write("S," + sendIp + "," + seqNo + "\n");

    val spString = sendIp.split(":")
    val nd = Node(spString(0), spString(1).toInt)
    val tempdaemonactor = select(nd, 'daemon)
    tempdaemonactor ! msgFinal(ip, seqNo, msg)
    bw.close()

  }

  def recvMsg() = {

    react {
      case msgFinal(senderIp, sno, msg) => {
        val bw = new BufferedWriter(new FileWriter(ip + ".sclog", true))
        bw.write("R," + senderIp + "," + sno + "\n");
        println(ip + " received from  " + senderIp)
        bw.close()

        val r = new Random()
        val x = r.nextInt(addrList.size)

        // if (nomsgs <= 1000) {
        println(ip + " sending to  " + addrList(x))
        sendMsg(addrList(x), seqNo, "Sending msg...")
        seqNo = seqNo + 1
        nomsgs = nomsgs + 1
        //}

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
      recvMsg()
    } //loop  
  } //act

} //class

object actorsStart {
  def main(args: Array[String]) {

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
      tempdaemonactor ! msgFinal("obj", 0, "Starting")
    }

        receiveWithin(300) {
      case TIMEOUT => {
        println("   WATCHOUT !  ")
        Thread.sleep(100)
      }
    }
  }
}

object reviewActor {
  def main(args: Array[String]) {

    val currdir = new File(".").getCanonicalPath();
    //parentdirectory val dir2 = new File("..");
    val path = new File(currdir)

    val files = path.listFiles();

    for (i <- 0 until files.length) {
      if (files(i).isFile()) { //this line weeds out other directories/folders
        //println(files(i));
        var extnsn = files(i).toString().substring(files(i).toString().lastIndexOf(".") + 1, files(i).toString().length());

        if (extnsn.equals("sclog")) {

          // println("Yes")
          val br = new BufferedReader(new FileReader(files(i).toString()))

          var str = br.readLine()
          str = br.readLine()
          str = br.readLine() //Ignore line 1

          do {
            //println(str)

            if (str.charAt(0) == 'S') {
              val cntnt = str.split(",")
              var found = 0

              var currfileip = files(i).toString().substring(files(i).toString().lastIndexOf("/") + 1, files(i).toString().length());
              currfileip = currfileip.replace(".sclog", "")
              //println(currfileip)

              var srchFile = files(i).toString().substring(0, files(i).toString().lastIndexOf("/") + 1)
              srchFile = srchFile + cntnt(1) + ".sclog"
              //println(srchFile)

              val brSrch = new BufferedReader(new FileReader(srchFile))
              var srchstr = brSrch.readLine() //Ignore line 1

              do {

                if (srchstr.equals("R," + currfileip + "," + cntnt(2))) {
                  found = found + 1;
                }

                srchstr = brSrch.readLine()

              } while (srchstr != null)

              if (found == 0) {
                println("ERROR ! : Message from " + currfileip + " with sequence no. " + cntnt(2) + " was not received by " + cntnt(1))
              } else if (found == 1 && args.length > 0 && args.contains("-v")) {
                println("Msg by " + str + " received!")
              } else if (found > 1 && args.length > 0 && args.contains("-s")) {
                println("Impossible ! : Malicious Entities at play !")
              }

              brSrch.close()

            } //if_S

            str = br.readLine()
          } while (str != null)

          br.close()
        }
      }
    }
  } //main
} //obj

object killActor {
  def main(args: Array[String]) {

    val spString = args(0).split(":")
    val nd = Node(spString(0), spString(1).toInt)
    val tempdaemonactor = select(nd, 'daemon)
    tempdaemonactor ! msgDie()

  }
}*/