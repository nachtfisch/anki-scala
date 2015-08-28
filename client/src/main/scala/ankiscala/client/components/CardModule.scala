package ankiscala.client.components

import ankiscala.client.services.{Card, LearnCardsStore}
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactElement}


object CardModule {

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
