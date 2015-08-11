package de.nachtfische.web


import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import de.nachtfische.ankimodel._
import org.json4s.{DefaultFormats, Formats}
import spray.can.Http
import spray.http.MediaTypes
import spray.httpx.Json4sJacksonSupport
import spray.routing.HttpServiceActor

import scala.concurrent.duration._

object Main extends App {
    implicit val system = ActorSystem("on-spray-can")

    // create and start our service actor
    val service = system.actorOf(Props[RestInterface], "demo-service")

    implicit val timeout = Timeout(5.seconds)
    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Http) ? Http.Bind(service, interface = "localhost", port = 8088)
}

case class QuestionAnswerPair(question: String, answer: String)

class RestInterface extends HttpServiceActor with Json4sJacksonSupport {

    def receive = runRoute(routes)

    def ankiManager = Anki

    override implicit def json4sJacksonFormats: Formats = DefaultFormats

    val completeWithJson = respondWithMediaType(MediaTypes.`application/json`) & complete

    val routes =
        path("api" / "decks") {
            get {
                completeWithJson {
                    ankiManager.apkgMap.map { t => t._1 }
                }
            }
        } ~
          path("api" / "decks" / Segment) { id =>
              get {
                  completeWithJson {
                      fetchNotes(id)
                  }
              }
          } ~ path("api" / "decks" / "download") {
            post {
                entity(as[Map[String, String]]) { requestMap =>
                    completeWithJson {
                        val id: String = ankiManager.fetchApkg(requestMap("url"))
                        Map[String, String]("id" -> id)
                    }
                }
            }
        } ~ path("app") {
            getFromResource("webapp/index.html")
        } ~ pathPrefix("app" / "js") {
            getFromResourceDirectory("webapp/js")
        }

    def fetchNotes(id: String): Seq[QuestionAnswerPair] = {
        val apkg: ApkgFile = Anki.getById(id).getOrElse(Anki.sampleApkg)
        val facts = apkg.notes
        val allCards: Seq[(String, String)] = (facts map MustacheRenderer.renderFact).flatten

        allCards map QuestionAnswerPair.tupled
    }

}