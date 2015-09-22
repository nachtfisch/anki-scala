package ankiscala.client.components

import ankiscala.client.services.LearnCardsStore
import ankiscala.services.Card
import japgolly.scalajs.react.{ReactElement, ReactComponentB}
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._

object LearningModule {
  val Component = ReactComponentB[RouterCtl[Pages]]("LearnList")
    .initialState(LearnCardsStore.LearnCardStream)
    .render(renderLearningView)
    .build

  def renderLearningView: (RouterCtl[Pages], Set[Card]) => ReactElement = { (props, state) =>
      <.div("learned cards #" + state.size,
        state map renderCard,
        props.link(SearchCardsPage)("Choose more cards")
      )
  }

  def renderCard(c: Card) = {
    <.div(s"${c.front} - ${c.back}",
      <.button("learned", ^.onClick --> {
        LearnCardsStore.scheduleForRemembering(c)
      }))
  }
}
