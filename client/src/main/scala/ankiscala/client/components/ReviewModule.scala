package ankiscala.client.components

import ankiscala.client.services.ReviewStore.CardReviewItem
import ankiscala.client.services.{Card, ReviewStore}
import ankiscala.services.ReviewItem
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactElement}
import rx._
import rx.ops._

object ReviewModule {

  case class Ease(label:String, value:Int)

  val answerOptions = Seq(Ease("easy", 4), Ease("normal", 3), Ease("hard",2))

  abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {

    protected def observe[T](rx: Rx[T]): Unit = {
      val obs = rx.foreach(_ => scope.forceUpdate())
      // stop observing when unmounted
      onUnmount(obs.kill())
    }

  }

  case class Props(todos: Rx[Seq[CardReviewItem]], router: RouterCtl[Pages])

  class Backend(t: BackendScope[Props, Unit]) extends RxObserver(t) {
    def mounted(): Unit = {
      observe(t.props.todos)

      ReviewStore.refreshReviews()
    }

    def reviewItem(cardReviewItem: CardReviewItem, ease:Int) = {
      ReviewStore.reviewCard(cardReviewItem, ease)
    }
  }

  val Component = ReactComponentB[Props]("Review")
    .stateless
    .backend(new Backend(_))
    .render((props, _ , backend) => renderHeadIfPresent(props.todos(), backend))
    .componentDidMount(t => t.backend.mounted())
    .configure(OnUnmount.install)
    .build


  def renderHeadIfPresent(cards: Seq[CardReviewItem], backend: Backend): ReactElement = {
    cards.headOption match {
      case Some(c) => <.div(s"count #${cards.size} ${c.review.due} ${c.review.reviewProgress}", renderReview(c, backend))
      case None => <.p("nothing to review")
    }
  }

  def renderReview(c:CardReviewItem, backend: Backend): ReactElement = {
    <.div(
      <.div(s"${c.card.front} -- ${c.card.back}"),
      <.div(<.button("didn't know"), answerOptions map { t => <.button(t.label, ^.onClick --> backend.reviewItem(c, t.value))})
    )
  }
}
