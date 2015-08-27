package de.nachtfische.services

import ankiscala.client.services.AjaxClient
import ankiscala.services.API
import autowire._
import boopickle.Default._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow


case class Card(id:String, front:String, back:String)

trait CardStore {

  var cards = Seq.empty[Card]

  def addCard(card: Card): Unit = {

  }

  def refreshCards:Unit = {
    AjaxClient[API].getCards().call().map( newCards =>
      cards = newCards map (c => Card(c.id, c.questionAnswerPair.answer, c.questionAnswerPair.question))
    )
  }

}

object CardStore extends CardStore {

}
