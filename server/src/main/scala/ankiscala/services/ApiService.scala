package ankiscala.services

import java.util.UUID

import de.nachtfische.sampledata.SpanishCardGenerator
import de.nachtfische.srs.AnkiDroidSrsAlgorithm
import org.joda.time.{DateTime, Period}


class ApiService extends API {

   var cards = SpanishCardGenerator.spanishCards
   var reviews = Seq.empty[ReviewItem]

   override def getCards(): Seq[FlashCard] = {
     cards.take(20)
   }

   override def updateReview(reviewId: String, ease: Int, time:Long): Unit = {

     val updatedReviewItem: Option[(ReviewItem, ReviewState)] = for {
       reviewItem <- reviews.find(_.id == reviewId)
       newState: ReviewState = newReviewState(time, ease, reviewItem.due, reviewItem.reviewProgress)
     } yield (reviewItem, newState)

     updatedReviewItem match {
       case None => println("couldn't update no item found")
       case Some((item, newState)) =>
         cards.collect {
           case i if i.id == item.id => item.copy(reviewProgress = newState, due = calculateDue(newState, DateTime.now()).getMillis)
           case i => i
         }
         println(s"updated $item")
     }

   }

   private def newReviewState(time: Long, ease: Int, due: Long, progress: ReviewState): ReviewState = {
     val delay: Int = Math.max(new Period(due, time).getDays, 0) // early review case

     AnkiDroidSrsAlgorithm.review(progress, delay, ease)
   }

  private def calculateDue(state:ReviewState, fromDate: DateTime): DateTime = {
    fromDate.plus(Period.days(state.level))
   }

   override def getReviews(userId: String): Seq[ReviewItem] = {
     reviews
   }

   override def addReview(factId: String): Seq[ReviewItem] = {
     reviews :+= ReviewItem(UUID.randomUUID().toString, factId, ReviewState.InitialReviewState, DateTime.now().getMillis)
     reviews
   }

 }
