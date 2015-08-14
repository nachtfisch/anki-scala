package de.nachtfische.web
import java.util.UUID

import de.nachtfische.srs.{AnkiDroidSrsAlgorithm, ReviewState}
import org.joda.time.{DateTime, Period}
import spray.httpx.Json4sJacksonSupport
import spray.routing.{HttpServiceBase, MalformedRequestContentRejection}

import scala.collection.mutable

case class CreateReviewItemRequest(factId:String)

trait StudyRoutes extends HttpServiceBase with RouteUtils with Json4sJacksonSupport {

    val reviewRepository:mutable.MutableList[ReviewItem]

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
}
