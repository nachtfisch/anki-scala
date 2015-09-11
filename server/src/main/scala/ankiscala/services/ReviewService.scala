package ankiscala.services

import de.nachtfische.srs.AnkiDroidSrsAlgorithm
import org.joda.time.{Period, DateTime}

object ReviewService {

    type ReviewId = String

    sealed trait ReviewEvent {
        def id: ReviewId
    }
    case class FactAdded(id: ReviewId, factId: String) extends ReviewEvent
    case class FactReviewed(id: ReviewId, reviewTime: Long, ease: Int) extends ReviewEvent

    case class ReviewItems(byId: Map[ReviewId, ReviewItem] = Map.empty[ReviewId, ReviewItem]) {

        def apply(event: ReviewEvent): ReviewItems = event match {
            case event: FactAdded =>
                handle(event)
            case event: FactReviewed =>
                handle(event)
        }

        def handle(event: FactReviewed): ReviewItems = {
            val oldReview: ReviewItem = byId(event.id)
            val newState: ReviewState = newReviewState(event.reviewTime, event.ease, oldReview.due, oldReview.reviewProgress)
            val newReviewItem: ReviewItem = oldReview.copy(reviewProgress = newState,
                due = calculateDue(newState, new DateTime(event.reviewTime)).getMillis)

            this.copy(byId.updated(event.id, newReviewItem))
        }

        def handle(event: FactAdded): ReviewItems = {
            val initialReviewState: ReviewState = ReviewState.InitialReviewState
            val item: ReviewItem = ReviewItem(event.id,
                event.factId,
                initialReviewState,
                calculateDue(initialReviewState, DateTime.now()).getMillis)

            this.copy(byId.updated(event.id, item))
        }

        private def calculateDue(state: ReviewState, fromDate: DateTime): DateTime = {
            fromDate.plus(Period.days(state.level))
        }

        private def newReviewState(time: Long, ease: Int, due: Long, progress: ReviewState): ReviewState = {
            val delay: Int = Math.max(new Period(due, time).getDays, 0) // early review case

            AnkiDroidSrsAlgorithm.review(progress, delay, ease)
        }

    }

    object ReviewItems {
        def fromEventStream(events: ReviewEvent*) = {
            events.foldLeft(ReviewItems())(_.apply(_))
        }
    }

}
