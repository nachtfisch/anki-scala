package ankiscala.client.services

import ankiscala.services.{API, FlashCard}
import autowire._
import boopickle.Default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

import scala.concurrent.Future

object ReviewStore {
  var reviewList = Seq.empty[Card]

  def refreshReviews() = {
    val cardsFuture: Future[Seq[Card]] = for {
      reviews <- AjaxClient[API].getReviews("userA").call()
      cards <- AjaxClient[API].getCards().call()
    } yield {
        cards
          .filter(c => !reviews.filter(_.factId == c.id).isEmpty)
          .map(mapToCard)
      }
    cardsFuture.map( list => reviewList = list)
  }

  def mapToCard: (FlashCard) => Card = {
    c => Card(c.id, c.questionAnswerPair.question, c.questionAnswerPair.answer)
  }

}
