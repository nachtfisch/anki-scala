package de.nachtfische.services

case class Card(id:String, front:String, back:String)

trait CardStore {

  private val cards = scala.collection.mutable.MutableList(Card("a","b","c"))

  def addCard(card: Card): Unit = {
    cards += card
  }

  def getCards:List[Card] = {
    cards.toList
  }

}

object CardStore extends CardStore {

}
