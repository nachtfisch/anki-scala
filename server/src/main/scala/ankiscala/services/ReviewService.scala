package ankiscala.services

import de.nachtfische.srs.AnkiDroidSrsAlgorithm
import org.joda.time.{Period, DateTime}

object ReviewService {

    type ReviewId = String

    sealed trait ReviewEvent {
        def id: ReviewId
    }
    case class FactAdded(id: ReviewId, factId: String) extends ReviewEvent
    case class FactReviewed(id: ReviewId, reviewedAt: Long, ease: Int) extends ReviewEvent

    case class ReviewItems(
        byId: Map[ReviewId, ReviewItem] = Map.empty,
        uncommitedEvents: List[ReviewEvent] = List.empty) {

        def apply:PartialFunction[ReviewEvent, ReviewItems] = {
            case event @ FactAdded(_,_) =>
                applyEvent(event).copy(uncommitedEvents = uncommitedEvents :+ event)
            case event: FactReviewed =>
                applyEvent(event).copy(uncommitedEvents = uncommitedEvents :+ event)
        }

        def applyEvent(event: FactReviewed): ReviewItems = {
            val newReviewItem: ReviewItem = updateReviewItem(event.id, event.reviewedAt, event.ease)

            this.copy(byId.updated(event.id, newReviewItem))
        }

        def updateReviewItem(id: ReviewId, at: Long, ease: Int): ReviewItem = {
            val oldReview: ReviewItem = byId(id)
            val newState: ReviewState = newReviewState(at, ease, oldReview.due, oldReview.reviewProgress)
            val newReviewItem: ReviewItem = oldReview.copy(reviewProgress = newState,
                due = calculateDue(newState, new DateTime(at)).getMillis)
            newReviewItem
        }

        def applyEvent(event: FactAdded): ReviewItems = {
            val initialReviewState: ReviewState = ReviewState.InitialReviewState
            val item: ReviewItem = ReviewItem(event.id,
                event.factId,
                initialReviewState,
                calculateDue(initialReviewState, DateTime.now()).getMillis)

            this.copy(byId.updated(event.id, item))
        }

        def markCommitted = copy(uncommitedEvents = List.empty)

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
            events.foldLeft(ReviewItems())(_ apply _ )
        }
    }

}
