package ankiscala.client.components

import ankiscala.client.services.{Card, ReviewStore}
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactElement}
import rx._
import rx.ops._

object ReviewModule {

  val answerOptions = Seq("easy", "normal", "hard")

  abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {

    protected def observe[T](rx: Rx[T]): Unit = {
      val obs = rx.foreach(_ => scope.forceUpdate())
      // stop observing when unmounted
      onUnmount(obs.kill())
    }

  }

  case class Props(todos: Rx[Seq[Card]], router: RouterCtl[Pages])

  class Backend(t: BackendScope[Props, Unit]) extends RxObserver(t) {
    def mounted(): Unit = {
      observe(t.props.todos)

      ReviewStore.refreshReviews()
    }
  }

  val Component = ReactComponentB[Props]("Review")
    .stateless
    .backend(new Backend(_))
    .render((props, _ , _) => renderHeadIfPresent(props.todos()))
    .componentDidMount(t => t.backend.mounted())
    .configure(OnUnmount.install)
    .build


  def renderHeadIfPresent(cards: Seq[Card]): ReactElement = {
    cards.headOption match {
      case Some(c) => <.div(s"count #${cards.size}", renderReview(c))
      case None => <.p("nothing to review")
    }
  }

  def renderReview(c:Card): ReactElement = {
    <.div(
      <.div(s"${c.front} -- ${c.back}"),
      <.div(<.button("didn't know"), answerOptions map { t => <.button(t)})
    )
  }
}
