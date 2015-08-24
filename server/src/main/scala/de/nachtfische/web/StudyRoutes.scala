package de.nachtfische.web
import java.util.UUID

import ankiscala.services.{ReviewItem, ReviewState}
import spray.httpx.Json4sJacksonSupport
import spray.routing.HttpServiceBase

import scala.collection.mutable

case class ReviewRequest(ease:Int, reviewTime:Long)


case class CreateReviewItemRequest(factId:String)

trait StudyRoutes extends HttpServiceBase with RouteUtils with Json4sJacksonSupport {

    val reviewRepository:mutable.MutableList[ReviewItem]

    val learnRoutes = pathPrefix("api" / "study" / "reviews") {
        (pathEnd & post) {
            entity(as[CreateReviewItemRequest]) { request =>
                completeWithJson {
                    val reviewId: UUID = UUID.randomUUID()
                    val initialState: ReviewState = ReviewState.InitialReviewState
                    reviewRepository += ReviewItem(reviewId.toString, request.factId, initialState, 0)
                    Map("id" -> reviewId.toString)
                }
            }
        } ~ (pathEnd & get) {
            // retrieve all reviews
            completeWithJson(reviewRepository)
        }
    }

}
