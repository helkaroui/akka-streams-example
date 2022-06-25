package dev.sharek.examples

import akka.{Done, NotUsed}
import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem
import akka.util.ByteString

import java.nio.file.Paths
import scala.concurrent.{ExecutionContextExecutor, Future}

object Main {
  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("QuickStart")
    implicit val ec: ExecutionContextExecutor = system.dispatcher


    /** Simple Pattern : **/
    // Source => Flow => Sink

    val source: Source[Int, NotUsed] = Source(1 to 100)

    val flow: Flow[Int, String, NotUsed] = Flow[Int]
      .filter(_ % 2 == 0)
      .map(v => (v, (1 to v).toList))
      .expand{case (v, r) => r.map(x => s"${v}_${x}").iterator}

    val sink = Sink.foreach[String](println)

    val done: Future[Done] = source
      .via(flow)
      .runWith(sink)



    /** Customize & compose reusable pieces **/
    // Creating custom Source
    val customSource: Source[String, NotUsed] = source.via(flow)

    // Creating a custom Sink :
    val loggerSink: Sink[String, Future[IOResult]] =
      Flow[String]
        .map(s => ByteString(s + "\n"))
        .toMat(FileIO.toPath(Paths.get("tmp.log")))(Keep.right)

    customSource.runWith(loggerSink)
  }
}
