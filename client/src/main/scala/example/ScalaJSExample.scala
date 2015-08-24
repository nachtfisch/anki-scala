package example

import de.nachtfische.services.{CardStore, Card}
import org.scalajs.dom.html
import org.scalajs.dom.html.LI

import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._


@JSExport
object ScalaJSExample {
  @JSExport
  def main(target: html.Div): Unit = {
    CardStore.addCard(Card("id1", "front", "back"))
    val cards = CardStore.getCards
    val (animalA, animalB) = ("fox", "cat")
    target.appendChild(
      div(
        h1("Hello World!"),
        p(
          "The quick brown ", b(animalA),
          " jumps over the lazy ",
          i(animalB), "."
        ),
        ul(renderCards(cards))
      ).render
    )
  }

  def renderCards(list:Seq[Card]): Seq[TypedTag[LI]] = {
    list.map { e => li(e.id)}
  }

}