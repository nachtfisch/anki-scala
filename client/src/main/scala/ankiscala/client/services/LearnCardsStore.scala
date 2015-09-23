package ankiscala.client.services

import ankiscala.client.AnkiScalaMain
import ankiscala.services.{Card, API}

import autowire._
import boopickle.Default._

import rx._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

object LearnCardsStore {

  var availableCards = Var(Seq.empty[Card])

  def refreshAvailableCards(): Unit = {
    AjaxClient[API].getCardSuggestions(getUser)
      .call()
      .map(newCards => availableCards() = newCards)
  }

  def scheduleForRemembering(c: Card) = {
    AjaxClient[API].newReview(getUser, c.id)
      .call()
      .map(_ => removeCard(c.id))
      .andThen({ case _ => ReviewStore.refreshReviews() })
  }


  def removeCard(id: String): Unit = {
    availableCards() = availableCards().filterNot(_.id == id)
  }

  def getUser: String = {
    AnkiScalaMain.userId().get
  }

}
