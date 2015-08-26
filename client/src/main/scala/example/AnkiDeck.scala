package example

import de.nachtfische.services.{Card, CardStore}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router2._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

sealed trait Pages

case object Cards extends Pages

case object MarkedForLearning extends Pages

case object Review extends Pages

object MainMenu {
  val pages = Seq(Cards, MarkedForLearning, Review)
  val Component = ReactComponentB[RouterCtl[Pages]]("Menu")
  .render((props) =>
    <.div(<.ul( pages map {t => <.li(props.link(t)(t.getClass.getName))}))
    ).build
}

object LearnView {
  val Component = ReactComponentB[RouterCtl[Pages]]("LearnList")
  .initialState(CardStore.getCards)
  .render((props,state) =>
    <.div("learned cards #" + state.size,
      state map renderCard,
      props.link(Cards)("Choose more cards")
    )
    ).build

  def renderCard(c:Card) = {
    <.div(s"${c.front} - ${c.back}",
      <.button("learned", ^.onClick --> {ReviewModule.reviewList :+= c}))
  }
}



object ReviewModule {

  val options = Seq("easy", "normal", "hard")

  var reviewList = Seq.empty[Card]

  val Component = ReactComponentB[RouterCtl[Pages]]("Review")
    .initialState(reviewList)
  .render((props, state) => state.headOption match {
    case Some(c) => <.div(s"count #${state.size}", renderReview(c))
    case None => <.p("nothing to render")
  }).build


  def renderReview(c:Card): ReactElement = {
    <.div(
      <.div(s"${c.front} -- ${c.back}"),
      <.div(<.button("didn't know"), options map { t => <.button(t)})
    )
  }
}


object CardView {

  type CardProps = RouterCtl[Pages]

  class Backend(t: BackendScope[CardProps, Int]) {

    def addCard(card: Card): Unit = {
      CardStore.addCard(card)
      t.setState(CardStore.getCards.size)
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
    <.div(<.p(s"count $state"), ol)
  }

  val CardComponent = ReactComponentB[CardProps]("Cards")
    .initialState(CardStore.getCards.size)
    .backend(new Backend(_))
    .render(renderCards)
    .build

  def renderCards(backend: Backend, list: Seq[Card]): Seq[ReactTag] = {
    list.map { e => <.li(s"${e.id} front: ${e.front}", <.button(
      ^.onClick --> {
        backend.addCard(e)
      },
      "Add for learning!"))
    }
  }

}

@JSExport
object AnkiDeck extends JSApp {

  @JSExport
  def main(): Unit = {
    val routerConfig = RouterConfigDsl[Pages].buildConfig { dsl =>
      import dsl._
      (emptyRule
        | staticRoute(root, Cards) ~> renderR(rt => CardView.CardComponent(rt))
        | staticRoute("#review", Review) ~> renderR(rt => ReviewModule.Component(rt))
        | staticRoute("#card", MarkedForLearning) ~> renderR(rt => LearnView.Component(rt))
        ).notFound(redirectToPage(Cards)(Redirect.Replace))
      .renderWith((router, content) => <.div(
        <.div(MainMenu.Component(router)), content.render()
      )
        )

    }

    val baseUrl = BaseUrl.fromWindowOrigin_/
    val router = Router(baseUrl, routerConfig.logToConsole)

    React.render(router(), dom.document.getElementById("root"))
  }


}


