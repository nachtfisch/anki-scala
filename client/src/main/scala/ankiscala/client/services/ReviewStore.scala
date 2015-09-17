package ankiscala.client.services

import ankiscala.services.{Card, ReviewItem, API}
import autowire._
import boopickle.Default._
import rx.core.Var

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

import scala.concurrent.Future
import scala.scalajs.js.Date

object ReviewStore {

    var reviewList = Var(Seq.empty[CardReviewItem])

    case class CardReviewItem(review: ReviewItem, card: Card)

    def refreshReviews() = {
        val cardsFuture: Future[Seq[CardReviewItem]] = for {
            reviews <- AjaxClient[API].getReviews("userA", new Date().getTime().toLong).call()
            cards <- AjaxClient[API].getCards().call()
        } yield {
                reviews.map(reviewItem => {
                    val card: Card = cards.find(_.id == reviewItem.factId).get
                    CardReviewItem(reviewItem, card)
                })
            }

        cardsFuture.map(list => reviewList() = list)
    }

    def reviewCard(c: CardReviewItem, ease: Int) = {
        AjaxClient[API].updateReview(c.review.id, ease, new Date().getTime().toLong)
          .call()
          .andThen { case _ => refreshReviews() }
    }

}
