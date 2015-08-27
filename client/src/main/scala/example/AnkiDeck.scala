package example

import ankiscala.client.services.AjaxClient
import ankiscala.services.{FlashCard, API}
import de.nachtfische.services.{Card, CardStore}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router2._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import autowire._
import boopickle.Default._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow


import scala.concurrent.Future
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

object LearnCardsStore {

  var cardsToLearn = Set.empty[Card]
  var availableCards = Seq.empty[Card]

  def addToLearn(c:Card) = {
    // TODO client check c not in review
    cardsToLearn += c
  }

  def refreshAvailableCards():Unit = {
    AjaxClient[API].getCards().call().map { newCards =>
      availableCards = newCards map ReviewStore.mapToCard
    }
  }

  def markAsLearned(c:Card) = {
    AjaxClient[API].addReview(c.id).call().map { _ => removeFromLearned(c) }
    ReviewStore.refreshReviews()
  }

  def removeFromLearned(c:Card) = {
    cardsToLearn = cardsToLearn.filterNot(_.id == c.id)
  }

}

object LearnView {
  val Component = ReactComponentB[RouterCtl[Pages]]("LearnList")
  .initialState(LearnCardsStore.cardsToLearn)
  .render((props,state) =>
    <.div("learned cards #" + state.size,
      state map renderCard,
      props.link(Cards)("Choose more cards")
    )
    ).build

  def renderCard(c:Card) = {
    <.div(s"${c.front} - ${c.back}",
      <.button("learned", ^.onClick --> {LearnCardsStore.markAsLearned(c)}))
  }
}

object ReviewStore {
  var reviewList = Seq.empty[Card]

  def refreshReviews() = {
    val cardsFuture: Future[Seq[Card]] = for {
      reviews <- AjaxClient[API].getReviews("userA").call()
      cards <- AjaxClient[API].getCards().call()
    } yield {
        cards
          .filter(c => !reviews.filter(_.factId == c.id).isEmpty)
          .map(mapToCard)
      }
    cardsFuture.map( list => reviewList = list)
  }

  def mapToCard: (FlashCard) => Card = {
    c => Card(c.id, c.questionAnswerPair.question, c.questionAnswerPair.answer)
  }
}

object ReviewModule {

  val options = Seq("easy", "normal", "hard")


  val Component = ReactComponentB[RouterCtl[Pages]]("Review")
    .initialState(ReviewStore.reviewList)
    .render((props, state) => renderHeadIfPresent(state))
    .componentDidMount(t => ReviewStore.refreshReviews())
    .build


  def renderHeadIfPresent(state: Seq[Card]): ReactElement = {
    state.headOption match {
      case Some(c) => <.div(s"count #${state.size}", renderReview(c))
      case None => <.p("nothing to render")
    }
  }

  def renderReview(c:Card): ReactElement = {
    <.div(
      <.div(s"${c.front} -- ${c.back}"),
      <.div(<.button("didn't know"), options map { t => <.button(t)})
    )
  }
}


object CardView {

  type CardProps = RouterCtl[Pages]

  class Backend(t: BackendScope[CardProps, Seq[Card]]) {

    def addCard(card: Card): Unit = {
      LearnCardsStore.addToLearn(card)
    }

  }

  private def renderCards: (CardProps, Seq[Card], Backend) => ReactElement = (routerCtl, state, backend) => {
    val ol: ReactElement = <.ol(
      ^.id := "my-list",
      ^.lang := "en",
      ^.margin := "8px",
      renderCards(backend, state))
    <.div(<.p(s"count ${state.size}"), ol)
  }

  val CardComponent = ReactComponentB[CardProps]("Cards")
    .initialState(LearnCardsStore.availableCards)
    .backend(new Backend(_))
    .render(renderCards)
    .componentDidMount(_ => LearnCardsStore.refreshAvailableCards())
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
    LearnCardsStore.refreshAvailableCards()

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


    React.render(router(),   dom.document.getElementById("root"))
  }


}


