package de.nachtfische.web


import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import de.nachtfische.ankimodel._
import de.nachtfische.srs.{AnkiDroidSrsAlgorithm, ReviewState}
import org.joda.time.{Period, DateTime}
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}
import spray.can.Http
import spray.http.{MediaTypes}
import spray.httpx.Json4sJacksonSupport
import spray.routing.{MalformedRequestContentRejection, HttpServiceActor}

import scala.collection.mutable
import scala.concurrent.duration._

object Main extends App {
    implicit val system = ActorSystem("on-spray-can")

    // create and start our service actor
    val service = system.actorOf(Props[RestInterface], "demo-service")

    implicit val timeout = Timeout(5.seconds)
    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Http) ? Http.Bind(service, interface = "localhost", port = 8088)
}

object CardRendering {
    case class QuestionAnswerPair(question: String, answer: String)
    case class Template(front: String, back: String)
    case class ModelField(name: String, value: String)
    case class ModelFact(fields: List[ModelField])
    case class RenderRequest(templates: List[Template], facts: List[ModelFact])
}

import CardRendering._

class RestInterface extends HttpServiceActor with Json4sJacksonSupport {

    def receive = runRoute(deckRoutes
      ~ learnRoutes
      ~ cardRoutes
      ~ staticRoutes)

    def ankiManager = Anki

    override implicit def json4sJacksonFormats: Formats = DefaultFormats ++
      org.json4s.ext.JavaTypesSerializers.all ++
      JodaTimeSerializers.all


    val completeWithJson = respondWithMediaType(MediaTypes.`application/json`) & complete

    val staticRoutes = path("app") {
        getFromResource("webapp/index.html")
    } ~ pathPrefix("app" / "js") {
        getFromResourceDirectory("webapp/js")
    }

    val cardRoutes = path("api" / "cards" / "render") {
        post {
            entity(as[RenderRequest]) { renderRequest =>
                if (renderRequest.facts.isEmpty || renderRequest.templates.isEmpty) {
                    reject(MalformedRequestContentRejection("can not be empty"))
                } else {
                    completeWithJson {
                        Map[String, String]("id" -> "successfull")
                    }
                }
            }
        }
    }

    case class Review(id: UUID, due: Long)
    case class ReviewItem(id:String, factId:String, reviewProgress:ReviewState, due:DateTime)
    case class ReviewRequest(ease:Int, reviewTime:Option[DateTime])
    case class CreateReviewItemRequest(factId:String)

    val reviewRepository = mutable.MutableList[ReviewItem]()

    reviewRepository += ReviewItem("sample", "oldFact", ReviewState.InitialReviewState, ReviewState.InitialReviewState.calculateDue(DateTime.now()))

    val learnRoutes = pathPrefix("api" / "study" / "reviews") {
        (pathEnd & post) {
            entity(as[CreateReviewItemRequest]) { request =>
                completeWithJson {
                    val reviewId: UUID = UUID.randomUUID()
                    val initialState: ReviewState = ReviewState.InitialReviewState
                    reviewRepository += ReviewItem(reviewId.toString, request.factId, initialState, initialState.calculateDue(DateTime.now))
                    Map("id" -> reviewId.toString)
                }
            }
        } ~ (pathEnd & get) {
            // retrieve all reviews
            completeWithJson(reviewRepository)
        } ~ pathPrefix(Segment) { id =>
            (get & pathEnd) {
                reviewRepository.find({ t =>
                    t.id.equals(id)
                }) match {
                    case None => reject(MalformedRequestContentRejection("not found"))
                    case Some(r) => completeWithJson(r)
                }
            } ~ (post & pathEnd) {

                entity(as[ReviewRequest]) { reviewRequest =>
                    val updatedState: Option[(ReviewItem, ReviewItem)] = reviewRepository.find({ t =>
                        t.id.equals(id)
                    }).map(t => {
                        val newReview: ReviewState =
                            newReviewState(reviewRequest.reviewTime, reviewRequest.ease, t.due, t.reviewProgress)
                        
                        (t, t.copy(reviewProgress = newReview, due = newReview.calculateDue(DateTime.now())))
                    })

                    updatedState match {
                        case None => reject(MalformedRequestContentRejection("review state not found"))
                        case Some((oldState, newState)) =>
                            val index = reviewRepository.indexOf(oldState)
                            reviewRepository.update(index, newState)
                            completeWithJson(newState)
                    }
                }
            }
        }
    }

    def newReviewState(time: Option[DateTime], ease: Int, due: DateTime, progress: ReviewState): ReviewState = {
        val reviewTime: DateTime = time.getOrElse(DateTime.now())
        val period: Period = new Period(due, reviewTime)
        val newReview: ReviewState = AnkiDroidSrsAlgorithm.review(progress, period.getDays(), ease)
        newReview
    }

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