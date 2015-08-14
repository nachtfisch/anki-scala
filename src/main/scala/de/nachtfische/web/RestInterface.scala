package de.nachtfische.web

object CardRendering {
    case class QuestionAnswerPair(question: String, answer: String)
    case class Template(front: String, back: String)
    case class ModelField(name: String, value: String)
    case class ModelFact(fields: List[ModelField])
    case class RenderRequest(templates: List[Template], facts: List[ModelFact])
}

import java.util.UUID

import de.nachtfische.ankimodel.{Anki, ApkgFile, MustacheRenderer}
import de.nachtfische.srs.{AnkiDroidSrsAlgorithm, ReviewState}
import de.nachtfische.web.CardRendering._
import org.joda.time.{DateTime, Period}
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}
import spray.http.MediaTypes
import spray.httpx.Json4sJacksonSupport
import spray.routing.{HttpServiceActor, MalformedRequestContentRejection}

import scala.collection.mutable


case class Review(id: UUID, due: Long)
case class ReviewItem(id:String, factId:String, reviewProgress:ReviewState, due:DateTime)
case class ReviewRequest(ease:Int, reviewTime:DateTime = DateTime.now())

case class CreateReviewItemRequest(factId:String)



class RestInterface extends HttpServiceActor with Json4sJacksonSupport {

    def receive = runRoute(deckRoutes
      ~ learnRoutes
      ~ cardRoutes
      ~ StaticRoutes.routes)

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

    def newReviewState(time: DateTime, ease: Int, due: DateTime, progress: ReviewState): ReviewState = {
        val delay: Int = Math.max(new Period(due, time).getDays, 0) // early review case

        AnkiDroidSrsAlgorithm.review(progress, delay, ease)
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