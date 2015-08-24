package de.nachtfische.web



import java.util.UUID

import ankiscala.services.{ReviewState, ReviewItem, QuestionAnswerPair}
import de.nachtfische.ankimodel.{Anki, ApkgFile, MustacheRenderer}
import org.joda.time.DateTime
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}
import spray.httpx.Json4sJacksonSupport
import spray.routing.{HttpServiceActor}

import scala.collection.mutable

class RestAPI
  extends HttpServiceActor
  with Json4sJacksonSupport
  with RouteUtils
  with StudyRoutes with CardRoutes {

    def receive = runRoute(deckRoutes
      ~ learnRoutes
      ~ cardRoutes
      ~ routes)

    val routes = path("app") {
        getFromResource("webapp/index.html")
    } ~ pathPrefix("app" / "js") {
        getFromResourceDirectory("public/javascripts")
    }

    def ankiManager = Anki

    override implicit def json4sJacksonFormats: Formats = DefaultFormats ++
      org.json4s.ext.JavaTypesSerializers.all ++
      JodaTimeSerializers.all


    val reviewRepository = mutable.MutableList[ReviewItem]()

    reviewRepository += ReviewItem("sample", "asdf ", ReviewState.InitialReviewState, 0)

    val deckRoutes =
        path("api" / "decks") {
            get {
                completeWithJson {
                    ankiManager.apkgMap.keys
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
        }

    def fetchNotes(id: String): Seq[QuestionAnswerPair] = {
        val apkg: ApkgFile = Anki.getById(id).getOrElse(Anki.sampleApkg)
        val facts = apkg.notes
        val allCards: Seq[(String, String)] = facts flatMap MustacheRenderer.renderFact

        allCards map QuestionAnswerPair.tupled
    }

}