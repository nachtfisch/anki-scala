package example

import de.nachtfische.services.Card
import japgolly.scalajs.react.React
import japgolly.scalajs.react.extra.router2.{BaseUrl, Router, Redirect, RouterConfigDsl}

import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom


import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object CardView {
  val Component = <.ol(
    ^.id := "my-list",
    ^.lang := "en",
    ^.margin := "8px",
    <.li("Item 1"),
    <.li("Item 2"),
    renderCards(Seq.empty[Card]))

  def renderCards(list: Seq[Card]): Seq[ReactTag] = {
    list.map { e => <.li("Item 1") }
  }
}


sealed trait Pages
case object SearchCards extends Pages
case object MarkedForLearning extends Pages
case object Review extends Pages

@JSExport
object AnkiDeck extends JSApp{
  @JSExport
  def main(): Unit = {
    val routerConfig = RouterConfigDsl[Pages].buildConfig { dsl =>
      import dsl._
      (
        staticRoute(root, SearchCards)  ~> render(CardView.Component)
        | staticRoute("#card", MarkedForLearning) ~> render(<.div("TODO"))
        ).notFound(redirectToPage(SearchCards)(Redirect.Replace))

    }
    val baseUrl = BaseUrl.fromWindowOrigin
    val router = Router(baseUrl, routerConfig)

    React.render(router(), dom.document.getElementById("root"))
  }


}


