import java.io._

object example {
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
              } else if (found == 1 &&  !args.contains("-v")) {
                println("Msg  "+ cntnt(2) + " by " + currfileip + "  was successfully received by  " + cntnt(1) )
              } else if (found > 1  && args.contains("-s")) {
                println("Impossible scenario ! : Malicious Entities at play ! : Multiple Msgs received for seq no " + cntnt(2) + " sent by " + currfileip)
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