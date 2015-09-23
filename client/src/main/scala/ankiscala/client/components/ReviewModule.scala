package ankiscala.client.components

import ankiscala.client.services.ReviewStore.CardReviewItem
import ankiscala.client.services.ReviewStore
import ankiscala.services.ReviewItem
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactElement}
import org.widok.moment.{Date, Moment}
import rx._
import rx.ops._

object ReviewModule {

  case class Ease(label:String, value:Int)

  val answerOptions = Seq(Ease("easy", 4), Ease("normal", 3), Ease("hard",2))

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
      case Some(c) => <.div(s"count #${cards.size} ${c.review.reviewProgress}", renderReview(c, backend))
      case None => <.p("nothing to review")
    }
  }

  def renderReview(c:CardReviewItem, backend: Backend): ReactElement = {
    val due: Date = Moment(c.review.due.toDouble)
    <.div(
      <.div("time: " + due.fromNow()),
      <.div(s"${c.card.front} -- ${c.card.back}"),
      <.div(<.button("didn't know"), answerOptions map { t => <.button(t.label, ^.onClick --> backend.reviewItem(c, t.value))})
    )
  }
}
