package ankiscala.services

import java.util.UUID

import ankiscala.services.ReviewService._
import de.nachtfische.sampledata.SpanishCardGenerator


class ApiService extends API {

  var cards = SpanishCardGenerator.spanishCards
  var reviews = ReviewService.ReviewItems()

  override def getCards(): Seq[FlashCard] = {
    cards.take(20)
  }

  override def updateReview(reviewId: String, ease: Int, time: Long): Unit = {
    reviews = reviews.apply(FactReviewed(reviewId, time, ease))
  }


  override def getReviews(userId: String, until:Long): Seq[ReviewItem] = {
    reviews.byId.values.toSeq.sortBy(_.due)
  }

  override def addReview(factId: String): Unit = {
    reviews = reviews.apply(FactAdded(UUID.randomUUID().toString, factId))
  }

}
