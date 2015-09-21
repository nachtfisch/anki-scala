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
        val newItems: Future[Seq[CardReviewItem]] =
            AjaxClient[API].getReviews("userA", in10Days.getTime().toLong)
              .call()
              .flatMap(Future.traverse(_)(fetchCardReviewItem))

        newItems.onSuccess({case res => reviewList() = res})
    }

    def in10Days: Date = {
        val date: Date = new Date()
        date.setDate(date.getDate() + 10)
        date
    }

    def fetchCardReviewItem(review: ReviewItem): Future[CardReviewItem] = {
        AjaxClient[API].getCard(review.factId).call()
          .map(CardReviewItem(review, _))
    }

    def reviewCard(c: CardReviewItem, ease: Int) = {
        AjaxClient[API].updateReview(c.review.id, ease, new Date().getTime().toLong)
          .call()
          .andThen { case _ => refreshReviews() }
    }

}
