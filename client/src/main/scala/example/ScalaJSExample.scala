package example

import de.nachtfische.services.Card
import japgolly.scalajs.react.React

import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom


import scala.scalajs.js.annotation.JSExport


@JSExport
object ScalaJSExample {
  @JSExport
  def main(): Unit = {
    val markup = someMarkup

    React.render(markup, dom.document.getElementById("root"))
  }

  def someMarkup: ReactTag = {
    <.ol(
      ^.id := "my-list",
      ^.lang := "en",
      ^.margin := "8px",
      <.li("Item 1"),
      <.li("Item 2"),
      renderCards(Seq.empty[Card]))
  }

  def renderCards(list: Seq[Card]): Seq[ReactTag] = {
    list.map { e => <.li("Item 1") }
  }

}