package ankiscala.client.components

import ankiscala.client.services.{Card, ReviewStore}
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, ReactElement}

object ReviewModule {

  val answerOptions = Seq("easy", "normal", "hard")

  val Component = ReactComponentB[RouterCtl[Pages]]("Review")
    .initialState(ReviewStore.reviewList)
    .render((props, state) => renderHeadIfPresent(state))
    .componentDidMount(t => ReviewStore.refreshReviews())
    .build


  def renderHeadIfPresent(state: Seq[Card]): ReactElement = {
    state.headOption match {
      case Some(c) => <.div(s"count #${state.size}", renderReview(c))
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
