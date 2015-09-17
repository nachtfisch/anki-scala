package ankiscala.client.services

import ankiscala.services.API

import autowire._
import boopickle.Default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

object LearnCardsStore {

  var cardsToLearn = Set.empty[Card]
  var availableCards = Seq.empty[Card]

  def addToLearn(c: Card) = {
    // TODO client check c not in review
    cardsToLearn += c
  }

  def refreshAvailableCards(): Unit = {
    AjaxClient[API]
      .getCards().call()
      .map(newCards => availableCards = newCards map ReviewStore.mapToCard)
  }

  def markAsLearned(c: Card) = {
    AjaxClient[API].newReview(c.id).call().map { _ => removeFromLearned(c) }
    ReviewStore.refreshReviews()
  }

  def removeFromLearned(c: Card) = {
    cardsToLearn = cardsToLearn.filterNot(_.id == c.id)
  }

}
