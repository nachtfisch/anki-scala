package ankiscala.client.components

import ankiscala.client.services.ReviewStore.CardReviewItem
import ankiscala.client.services.{ReviewStore, LearnCardsStore}
import ankiscala.services.Card
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, ReactElement, ReactComponentB}
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import rx._

object LearningModule {
  case class Props(card: Rx[Option[Card]], router: RouterCtl[Pages])

  class Backend(t: BackendScope[Props, Unit]) extends RxObserver(t) {
    def mounted() = {
      observe(t.props.card)
    }
  }

  val Component = ReactComponentB[Props]("LearnList")
    .stateless
    .backend(new Backend(_))
    .render( (props, _, backend) =>
      <.div(
        props.card() match {
          case Some(c) => renderCard(c)
          case None => "nothing left to learn for today"
        }
      )
    )
    .componentDidMount(t => t.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def renderCard(c: Card) = {
    <.div(
      <.div(
        <.span(c.front)
      ),
      <.div(
        <.span(c.back)
      ),
      <.div(
        <.button("help me rememeber", ^.onClick --> {
          LearnCardsStore.scheduleForRemembering(c)
        }),
        <.button("ignore", ^.onClick --> {
          ReviewStore.ignoreFact(c.id)
        })
      )
    )
  }
}
