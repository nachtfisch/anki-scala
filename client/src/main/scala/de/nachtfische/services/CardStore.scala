package de.nachtfische.services

case class Card(id:String, front:String, back:String)

trait CardStore {

  private val cards = scala.collection.mutable.MutableList.empty[Card]

  def addCard(card: Card): Unit = {
    cards += card
  }

  def getCards:List[Card] = {
    cards.toList
  }

}

object CardStore extends CardStore {

}
