package example

import ankiscala.services.{QuestionAnswerPair, FlashCard}
import de.nachtfische.services.{CardStore, Card}
import japgolly.scalajs.react.ReactComponentC.ReqProps
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router2._

import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import org.scalajs.dom.raw.Element


import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

sealed trait Pages

case object Cards extends Pages

case object MarkedForLearning extends Pages

case object Review extends Pages

object CardView {

  type CardProps = RouterCtl[Pages]


  class Backend(t: BackendScope[CardProps, Int]) {

    def addCard(card: Card): Unit = {
      CardStore.addCard(card)
      t.setState(t.state + 1)
    }

  }

  private val card: String => Card = id =>
    Card(id, s"front$id", s"back$id")

  val cards = Seq(card("1"), card("2"), card("3"))

  private def renderCards: (CardProps, Int, Backend) => ReactElement = (routerCtl, state, backend) => {
    val ol: ReactElement = <.ol(
      ^.id := "my-list",
      ^.lang := "en",
      ^.margin := "8px",
      renderCards(backend, cards))
    <.div(<.p(s"count $state"), ol, routerCtl.link(MarkedForLearning)("go to learn view"))
  }

  val CardComponent = ReactComponentB[CardProps]("Cards")
    .initialState(0)
    .backend(new Backend(_))
    .render(renderCards)
    .build


  def onTextChange(card: Card)(e: ReactEventI): Unit = {
    CardStore.addCard(card)

  }

  def renderCards(backend: Backend, list: Seq[Card]): Seq[ReactTag] = {
    list.map { e => <.li(s"${e.id} front: ${e.front}", <.button(
      ^.onClick --> {
        backend.addCard(e)
      },
      "Press me!"))
    }
  }

}

@JSExport
object AnkiDeck extends JSApp {

  @JSExport
  def main(): Unit = {
    val routerConfig = RouterConfigDsl[Pages].buildConfig { dsl =>
      import dsl._
      (
        staticRoute(root, Cards) ~> renderR(rt => CardView.CardComponent(rt))
          | staticRoute("#card", MarkedForLearning) ~> renderR(rt => <.div("TODO"))
        ).notFound(redirectToPage(Cards)(Redirect.Replace))

    }

    val baseUrl = BaseUrl.fromWindowOrigin_/
    val router = Router(baseUrl, routerConfig.logToConsole)



    React.render(router(), dom.document.getElementById("root"))
  }


}


