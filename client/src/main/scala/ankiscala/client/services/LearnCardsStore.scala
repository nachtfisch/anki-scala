package ankiscala.client.services

import ankiscala.client.AnkiScalaMain
import ankiscala.services.{Card, API}

import autowire._
import boopickle.Default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

object LearnCardsStore {

  var LearnCardStream = Set.empty[Card]
  var availableCards = Seq.empty[Card]

  def addToLearn(c: Card) = {
    // TODO client check c not in review
    LearnCardStream += c
  }

  def refreshAvailableCards(): Unit = {
    AjaxClient[API]
      .getCardSuggestions(getUser).call()
      .map(newCards => availableCards = newCards)
  }

  def scheduleForRemembering(c: Card) = {
    AjaxClient[API].newReview(getUser, c.id).call().map { _ => removeFromLearned(c) }
    ReviewStore.refreshReviews()
  }

  def removeFromLearned(c: Card) = {
    LearnCardStream = LearnCardStream.filterNot(_.id == c.id)
  }

  def getUser: String = {
    AnkiScalaMain.userId().get
  }

}
