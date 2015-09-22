package ankiscala.services

import de.nachtfische.srs.AnkiDroidSrsAlgorithm
import org.joda.time.{Period, DateTime}

object ReviewService {

    type ReviewId = String
    type Time = Long

    sealed trait ReviewEvent
    case class FactAdded(id: ReviewId, factId: String, at:Time) extends ReviewEvent
    case class FactReviewed(id: ReviewId, reviewedAt: Time, ease: Int) extends ReviewEvent
    case class FactIgnored(factId:String, at:Time) extends ReviewEvent

    case class ReviewItems(
        byId: Map[ReviewId, ReviewItem] = Map.empty,
        ignoredFacts: Seq[(String, Option[ReviewItem])] = Seq.empty
        ) {

        def apply:PartialFunction[ReviewEvent, ReviewItems] = {
            case event: FactAdded =>
                applyEvent(event)
            case event: FactReviewed =>
                applyEvent(event)
            case FactIgnored(id, t) =>
                copy(byId = byId - id,
                    ignoredFacts = ignoredFacts.:+((id, byId.get(id))))
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
