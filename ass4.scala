/*import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import scala.actors.Actor._
import scala.actors._
import scala.math._
import scala.util.Random
import scala.io.Source._


import scala.collection.mutable._

import java.util.zip.CRC32;
import java.util.zip.Checksum;

case class msgMyId(str:String)
case class msgPingRemoteActor(sAdrr:String)
case class msgRemoteMaster(m: scala.actors.OutputChannel[Any])
case class msgRemoteActor(args: Array[String])
case class msgToDeliver(key:String,msg: String,nhops : java.lang.Integer)
case class msgTest()
case class msgManualAdd(str:String)
case class msgStartRouting(hashstr:String)
case class msgRouteMsg(str:String,hopcount:java.lang.Integer,typ:java.lang.Integer,orgn : String)
case class msgDie()
case class msgAYA()
case class msgIAA() 
case class msgBro(ip:String)
case class msgTellMe()
case class msgAnnounceMyself()
case class msgHeyWhozClosest(str:String)
case class msgHezClosest(st : String)
case class msgDetails(hcnt:java.lang.Integer)

class Daemon(myName: String, portno: Int) extends Actor {  
 
  var myIp =""
  var myId = "" ///Assigned Hash with IP
    
   val copies = 3
   val hlfsetsz = 16
     
  var matchIndex : Array[java.lang.Integer] = null
  var addrmap = scala.collection.mutable.HashMap.empty[String,String]
   
  var  leftls = ArrayBuffer[String]()
  var  rightls = ArrayBuffer[String]()
 
  var rtable = new Array[ArrayBuffer[String]](9)
  rtable(0) = ArrayBuffer[String]()
  rtable(1) = ArrayBuffer[String]()
  rtable(2) = ArrayBuffer[String]()
  rtable(3) = ArrayBuffer[String]()
  rtable(4) = ArrayBuffer[String]()
  rtable(5) = ArrayBuffer[String]()
  rtable(6) = ArrayBuffer[String]()
  rtable(7) = ArrayBuffer[String]()
  rtable(8) = ArrayBuffer[String]()
  
  
  var extIp = ""
  
  def writeToFile (str1 : String)=	 {
	  val source = fromFile("file.txt")
	  val lines = source.mkString
	  source.close ()
  }
    
  def idString (str1 : String):String =	 {
               
     var bytes:Array[Byte] = str1.getBytes()
               
     val checksum = new CRC32()
     checksum.update(bytes,0,bytes.length)
               
     var lngChecksum = checksum.getValue()
     var hex = java.lang.Long.toHexString(lngChecksum)
     //println(hex.length()) 
     if(hex.length < 8){
       while(hex.length < 8){
         hex += "0"
       }
       
     }
     
     return hex
    
  }
  
  
   def commonString (str1 : String, str2 : String):java.lang.Integer =	{
    
    var x= -1;
    
      do {
      x=x+1
  	  } while((x)<str1.length()  && (x)<str2.length() && str1.charAt(x)==str2.charAt(x))
 
     return x
  }  
   
   
   def randomString (str:String,m : java.lang.Integer):String =	{
	  	
     	var rstring = ""
     	for (i  <- 0 until m) {
     		rstring = rstring+str.charAt(i)
     	}
     
       val digits = "0123456789abcdef"
       var c:Char = '\0'
       val r = new Random()
       var x=0  
       
        for (i  <- 0 until (8-m)) {
            x = r.nextInt(16)
      	   c = digits.charAt(x)
            rstring = rstring + c 
        }
       
       return rstring
      }  
   
   
     def hexToLong (hex : String):Long =	{
 
       val digits = "0123456789abcdef"
       var valz :Long = 0l
       var c:Char = '\0'
       var d:Long = 0l
       
        for (i  <- 0 until hex.length()) {
            c = hex.charAt(i)
            d = digits.indexOf(c)
            valz = 16*valz + d
        }
       
       return valz
     } 
     
   def diffString (str1 : String, str2 : String):Long =	{
     
     var l1 = hexToLong(str1)
     var l2 = hexToLong(str2)
     
     var x= abs(l1-l2)
     
     return x
  } 
   
  def removeAddr(k : String)	{
	  addrmap-= k
	  leftls-=k
	  rightls-=k
	   val  x = commonString(myId,k)
      rtable(x) -= k 
  }

  def nearestGuy(destnaddr: String): String = {
    var x = commonString(destnaddr, myId)
    var dist: Long = Long.MaxValue

    var diffdist: Long = Long.MaxValue
    var ip: String = ""
    var codeId = ""

    while (rtable(x).size == 0 && x < 8) {
      x = x + 1
    }
    while (rtable(x).size == 0 && x > 0) {
      x = x - 1
    }

    rtable(x).foreach { k =>
      diffdist = diffString(destnaddr, k)

      if (diffdist < dist) {
        dist = diffdist
        codeId = k
        ip = addrmap(k)
      }

    }

    if (x - 1 >= 0) {
      rtable(x).foreach { k =>
        //addrmap.keySet.foreach { k =>

        diffdist = diffString(destnaddr, k)

        if (diffdist < dist) {
          dist = diffdist
          codeId = k
          ip = addrmap(k)
        }
      }
    }
    if (x + 1 <= 8) {
      rtable(x).foreach { k =>
        //addrmap.keySet.foreach { k =>

        diffdist = diffString(destnaddr, k)

        if (diffdist < dist) {
          dist = diffdist
          codeId = k
          ip = addrmap(k)
        }

      }
    }

    leftls.foreach { k =>
      diffdist = diffString(destnaddr, k)

      if (diffdist < dist) {
        dist = diffdist
        codeId = k
        ip = addrmap(k)
      }
    } //for each

    diffdist = diffString(myId, destnaddr)

    if (diffdist < dist) {
      dist = diffdist
      codeId = myId
      ip = addrmap(myId)
    }

    rightls.foreach { k =>
      diffdist = diffString(destnaddr, k)

      if (diffdist < dist) {
        dist = diffdist
        codeId = k
        ip = addrmap(k)
      }
    } //for each
    
  
   return codeId
  
  } //def nearest

  //Updating my own table after sending the msg for future use  
  def updateTables() {
    var q = 1
    while (q < 8) {
      if (rtable(q).size < copies) {
        var p = q
        while (rtable(p).size == 0 && p < 8) {
          p = p + 1
        }
        while (rtable(p).size == 0 && p > 0) {
          p = p - 1
        }

        val rstr = randomString(myId, q)
        //println(myId+"("+myIp+")"+" needs " + rstr)

        val askMan = nearestGuy(rstr)
        val ipAskMan = addrmap(askMan)
        val spString = ipAskMan.split(":")
        val nd = Node(spString(0), spString(1).toInt)
        val askD = select(nd, 'daemon)
        askD ! msgRouteMsg(rstr, 0, 2, myIp)
      }
      q = q + 1
    }

    val alp = "123456789abcdef"

    for (h <- 0 until alp.length()) {

      var tid = alp.charAt(h) + "0000000"
      tid = randomString(tid, 1)

      if (commonString(myId, tid) == 0) {

        var count = 0

        rtable(0).foreach { k =>
          if (commonString(k, tid) > 0) {
            count = count + 1
          }
        } //for each

        if (count < copies) {
          //println(myId + "(" + myIp + ")" + " needs " + tid)

          val askMan = nearestGuy(tid)
          val ipAskMan = addrmap(askMan)
          val spString = ipAskMan.split(":")
          val nd = Node(spString(0), spString(1).toInt)
          val askD = select(nd, 'daemon)
          askD ! msgRouteMsg(tid, 0, 2, myIp)
        } //if_count>copies
      } //if_not_same1

    } //h_loop
  
  
    if(leftls.size < hlfsetsz){
           leftls.foreach { k =>
                val spString = addrmap(k).split(":")
                val nd = Node(spString(0),spString(1).toInt)
                val tempdaemonactor = select(nd, 'daemon)
                tempdaemonactor !  msgTellMe()
      	   } //for each 	
    }
    
    if(rightls.size < hlfsetsz){
           rightls.foreach { k =>
                val spString = addrmap(k).split(":")
                val nd = Node(spString(0),spString(1).toInt)
                val tempdaemonactor = select(nd, 'daemon)
                tempdaemonactor !  msgTellMe()
      	   } //for each
    }
  
  }//def_update

  
  def act() {

   RemoteActor.classLoader = getClass().getClassLoader()
   alive(portno)
   register('daemon, self)
    
    loop {
      react {
        
        case msgAYA() => { sender ! msgIAA() }

      	case msgMyId(str) =>{
      	  myIp = str
      	  myId = idString(str)
      	  //println("Assigned : "+myId)      
      	  self ! msgManualAdd(str)
      	}
        
        
        case msgManualAdd(str) =>{
          val id = idString(str)      
          
          if(!addrmap.contains(id)){
            addrmap += (id -> str) //adding in keyset

            if (hexToLong(id) < hexToLong(myId)) {

              leftls += id
              //Added in the left set

              if (leftls.size > hlfsetsz) {

                var distk = Long.MinValue
                var mxdist = Long.MinValue
                var sk = ""

                leftls.foreach { k =>

                  distk = diffString(myId, k)

                  if (distk > mxdist) {
                    mxdist = distk
                    sk = k //smallest guy  chosen
                  }
                } //for each

                leftls -= sk

              }
          
         	 } //ifleft

         else if(hexToLong(id) > hexToLong(myId)){
         	
         	  rightls  += id
         	   //Added in the left set
         	  if (rightls.size > hlfsetsz) {
            
              var distk = Long.MinValue
              var mxdist = Long.MinValue
              var lknown = ""
              
              rightls.foreach { k =>

                distk = diffString(myId, k)

                if (distk > mxdist) {
                  mxdist = distk
                  lknown = k // guy  chosen
                }
              } //for each
              
              rightls -= lknown
              
            }
         } //else
          
          val  x = commonString(myId,id)
          
          
          rtable(x) += id
          
          if(x>0 && rtable(x).size > copies){
         
            val r = new Random()
            val e = r.nextInt(copies+1)
            
            rtable(x) -= rtable(x)(e)
          
          }
          else if ( x==0 ){
            var count = 0
            rtable(x).foreach { k =>	
              	if(commonString(k,id) > 0){
              	  count=count+1
              	}
             } //for each
             
             if(count>copies){
            	 val r = new Random()
                val e = r.nextInt(copies+1)
               
            	 var cnt =0
                var killr =""
                
               rtable(x).foreach { k =>	
               	
                 if(commonString(k,id) > 0){
                 		if(cnt == e){
                 			killr = k

                 		   cnt = copies+100  
                 		}
                 		cnt=cnt+1
                 	}
            	 } //    
              
            	 rtable(x) -= killr
             
             } //if_count>copies
          	}
          }      
        }//case manual_add
      
      case msgPingRemoteActor(strAdd) => {

      	val spString = strAdd.split(":")
      	
      	println(spString(0))
      	println(spString(1))
       	
      	val nd = Node(spString(0),spString(1).toInt)
      	
         val tempdaemonactor = select(nd, 'daemon)
          tempdaemonactor ! "Hi!"
        }
       
        
        case msgTest() => {
          
         for(i <- 0 to 8){
           print("\n  "+i +"  :")
            	for(j<- 0 until rtable(i).length){
            		print(" -> "+rtable(i)(j) )
            } 
             println("  : ("+rtable(i).size+")\n")
          }
         
         println( "Left Leafset : ")          
         for(i <- 0 until leftls.size){
            print(" -> "+ leftls(i) )
          } 
         println("  : ("+leftls.size+")\n")
         
         println( "Right Leafset : ")          
         for(i <- 0 until rightls.size){
            print(" -> "+ rightls(i) )
          } 
         println("  : ("+rightls.size+")\n")
          
          
          addrmap.keySet.foreach { k =>
          	println(addrmap(k))
          	}
          
          
       }//case      

        
         case msgStartRouting(daddr)=> {
           //println("\nstarting routing...")
           self ! msgRouteMsg(daddr,-1,0,myIp)
                      
         }
        
         
        case msgRouteMsg(destnaddr,hpcnt,typ,orgn) =>{
          
          if(!(sender.equals(self)) ) {sender ! msgIAA()}
          
      	  val incrhpcnt = hpcnt+1
          
      	
          if(typ==0){
         	 //println("\n ===> ( "+destnaddr+" ) "+   "  received by  " + myId +"("+addrmap(myId)+")" )
         	         
          }
          
          val codeId = nearestGuy(destnaddr)
          val ip = addrmap(codeId)         
          
          if(myId.equals(codeId)){
            
            val stId = idString(orgn)
            
            if(typ==0){
            	println("\nFrom : "+stId+" ("+addrmap(myId)+")\t  Hops :\t"+ incrhpcnt +"\nFinal   : "+ myId+" ("+addrmap(myId)+")" +"\nObj-id  : "+destnaddr)
            		
            	//self!msgDetails(incrhpcnt)
               	
            }
            else if(typ==1){
                val spString = orgn.split(":")
                val nd = Node(spString(0),spString(1).toInt)
      	       
                val tempdaemonactor = select(nd, 'daemon)
                tempdaemonactor ! msgBro(ip)
                
            }
            else if(typ==2){
                val spString = orgn.split(":")
                val nd = Node(spString(0),spString(1).toInt)
                val tempdaemonactor = select(nd, 'daemon)
                tempdaemonactor ! msgManualAdd(myIp)
            } 
   
          } //if codeId = myId
         else{
           	//println("\n( "+destnaddr+" )  Routed to : "+idString(ip) + "("+ip+")")
           	
           	val spString = ip.split(":")
           	val nd = Node(spString(0),spString(1).toInt)
           	val tempdaemonactor = select(nd, 'daemon)
           	//tempdaemonactor ! msgTest()
           	tempdaemonactor ! msgRouteMsg(destnaddr,incrhpcnt,typ,orgn)
           	
           	if(typ==0){
           	  receiveWithin(2000) {
           	 	 case msgIAA() =>{
           	 	 	//println("Done - "+myId)
           	 	 }
           	 	 case TIMEOUT => {
           	  		removeAddr(idString(ip)) 
           	 	 	self ! msgRouteMsg(destnaddr,incrhpcnt,typ,orgn)   //hop got incrmennted in case
         	    }
         	 }
           	}
           
            updateTables()  
            //Thread.sleep(1000) //let all msgs get received
          }  
    }

         
       case msgBro(strm) => {
      	 	println ( myIp +" : " + myId )
      	 	println (strm +" : "+idString(strm))
         
            val spString = strm.split(":")

           	val nd = Node(spString(0),spString(1).toInt)
      	
           	val tempdaemonactor = select(nd, 'daemon)
           	tempdaemonactor ! msgTellMe()
         
         
       }
       
       case msgTellMe() => {
      	   addrmap.values.foreach { k =>
      	   	sender ! msgManualAdd(k)
      	   } //for each 	
         
       }
       
       case msgAnnounceMyself() => {
          addrmap.values.foreach { k =>
            
            val spString = k.split(":")
            val nd = Node(spString(0),spString(1).toInt)
      	
            val tempdaemonactor = select(nd, 'daemon)
            tempdaemonactor ! msgManualAdd(myIp)
            
          } //for each 
         
       }
       
       
       case msgDie() => {
         exit()
       }
       
      case msg => { }//println("Received !   :   "+msg)} //Default case

      } //react
    } //loop
  } //act

  
  


} //class

object project3{
  def main(args: Array[String]) {
    
    println("Program Started...")
    
      	val startport= 2000
      	val endport = 2000+args(0).toInt-1
    
      	var blackports = ArrayBuffer[java.lang.Integer]() 
    
      	for(pno<- startport to endport){
      		val d = new Daemon("daemon", pno)
      		d.start
    
      		val ip = "127.0.0.1:"+pno
      		d ! msgAYA() 
   	
   	  receiveWithin(300) {
         	 case msgIAA() =>{
         	  }
         	  case TIMEOUT => {
         		  	 blackports += pno
         	  		 //println(self+"   WATCHOUT ! : Port no : "+ pno)
         	  		 Thread.sleep(100)
         	   }
         	} 	
   	
   	d!msgMyId(ip)
    }
  
  
   println("Number of Blackports ! : " + blackports.size)
  	for(i <- 0 until blackports.size){
  	  println(blackports(i))
  	}

    println("\nEstablishing the Network ... ")  
    
      for(pno<- startport to endport){
	   
	   	val nd = Node("127.0.0.1",pno)
	   	val tempd = select(nd, 'daemon)
	  
	   	for(i<- startport to endport){
	   		tempd ! msgManualAdd("127.0.0.1:"+i)
   	 }
	  
   }
    
   Thread.sleep(5000)
   println("\nProceeding to start routing Random ids for Objects ... ")
    
   
       val digits = "0123456789abcdef"
       var c:Char = '\0'
       val r = new Random()
       var x=0  
       var rstring =""
   
      for(k <- 0 until args(1).toInt){   
        for (z  <- startport to endport ){ 
         rstring =""
         for (i  <- 0 until 8) {
            x = r.nextInt(16)
      	   c = digits.charAt(x)
            rstring = rstring + c 
        }
  
        
         val nd = Node("127.0.0.1",z)
      	val tempdaemonactor = select(nd, 'daemon)
      	tempdaemonactor ! msgStartRouting(rstring) 
        
       }
       Thread.sleep(1000)     
  
     }  
    
  }//main
}//prj3



//scala daemonaddnew <my ip new > <ip of known guy>
object daemonaddnew {
  def main(args: Array[String]) {
    //println("Port No Used to submit work: ")
    
    val myIp = args(0)
    val ipString = args(0).split(":")
    val pno = ipString(1).toInt //readInt()
    val d = new Daemon("daemon", pno)
    d.start
    d!msgMyId(myIp)
  
    
    val kString = args(1).split(":")
    val nd = Node(kString(0),kString(1).toInt)  	
    val tempdaemonactor = select(nd, 'daemon)
    val addr = d.idString(myIp)
    
    tempdaemonactor ! msgRouteMsg(addr,0,1,myIp)  //known guy hashed
    
    Thread.sleep(5000)
    d ! msgTest() 
    Thread.sleep(1000)
    d ! msgAnnounceMyself()
    Thread.sleep(1000)
 }
}





//scala daemonsubmit <address of any group member> <hash-id destination>
object daemonsubmit {
  def main(args: Array[String]) {
   
    val kString = args(0).split(":")
       
    val nd = Node(kString(0),kString(1).toInt)
    
    val tempdaemonactor = select(nd, 'daemon)

    tempdaemonactor ! msgStartRouting(args(1))  

 }
} 

object daemonsubmit100 {
  def main(args: Array[String]) {
     
       val digits = "0123456789abcdef"
       var c:Char = '\0'
       val r = new Random()
       var x=0  
       var rstring =""
       
       val machIp = args(0)
      
       val pstart = args(1).toInt
       val pend = args(2).toInt //readInt()  
         
      
      for(k <- 0 until 100){   
       for (z  <- pstart to pend ){ 
        rstring =""
        for (i  <- 0 until 8) {
            x = r.nextInt(16)
      	   c = digits.charAt(x)
            rstring = rstring + c 
        }
  
        
         val nd = Node(machIp,z)
      	val tempdaemonactor = select(nd, 'daemon)
      	tempdaemonactor ! msgStartRouting(rstring) 
        
       }
       Thread.sleep(1000)     
  
      } 
  
  }
}


object daemonkill {
  def main(args: Array[String]) {

    val machip = args(0)  
	  val startport= args(1).toInt
	  val endport = args(2).toInt
    
	  
	   for(pno<- startport to endport){
	   
	   	val nd = Node(machip,pno)
	   	val tempd = select(nd, 'daemon)
	   	tempd ! msgDie()
   	 }
	  
   }
    
 }


// startport endport 
object daemonmaster {

  def main(args: Array[String]) {
   
  val machip = args(0)  
  val startport= args(1).toInt
  val endport = args(2).toInt
    
  var blackports = ArrayBuffer[java.lang.Integer]() 
    
    for(pno<- startport to endport){
      val d = new Daemon("daemon", pno)
   	d.start
    
   	val ip = machip+":"+pno
   	
   	d ! msgAYA() 
   	
   	  receiveWithin(300) {
         	 case msgIAA() =>{
         	  	//println("\tConfirmed ! < "+ pno + "\ts"+sender )
         	  }
         	  case TIMEOUT => {
         		  	 blackports += pno
         	  		 //println(self+"   WATCHOUT ! : Port no : "+ pno)
         	  		 Thread.sleep(1000)
         	   }
         	} 	
   	
   	d!msgMyId(ip)
    }
  
  
   println("Number of Blackports ! : " + blackports.size)
  	for(i <- 0 until blackports.size){
  	  println(blackports(i))
  	}

    println("\n === GO ! ===")
  }
  
  

  
}


object daemonformnetwork {

  def main(args: Array[String]) {
	 
	  val machip = args(0)  
	  val startport= args(1).toInt
	  val endport = args(2).toInt
    
	  val rmachip = args(3)  
	  val rstartport= args(4).toInt
	  val rendport = args(5).toInt
    
	  
	   for(pno<- startport to endport){
	   
	   	val nd = Node(machip,pno)
	   	val tempd = select(nd, 'daemon)
	  
	   	for(i<- rstartport to rendport){
	   		tempd ! msgManualAdd(rmachip+":"+i)
   	 }
	  
   }
	  
	  println("\nPlease wait for all messages to get delivered and then press Ctrl^C !")

	  
  	   for(pno<- startport to endport){
	   
	   	val nd = Node(machip,pno)
	   	val tempd = select(nd, 'daemon)
	  
	   	for(i<- rstartport to rendport){
	   		tempd ! msgTest()
   	 }
	  
   }

  
  }
}

*/