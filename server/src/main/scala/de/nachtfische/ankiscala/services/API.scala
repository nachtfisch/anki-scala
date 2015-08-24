package de.nachtfische.ankiscala.services

import java.util.UUID

import de.nachtfische.sampledata.SpanishCardGenerator
import de.nachtfische.srs.{AnkiDroidSrsAlgorithm, ReviewState}
import de.nachtfische.web.ReviewItem
import org.joda.time.{DateTime, Period}


trait API {

  def getCards(): Seq[FlashCard]

  def getReviews(userId:String): Seq[ReviewItem]

  def addReview(factId:String): Seq[ReviewItem]

  def updateReview(reviewId: String, ease: Int, time:DateTime): Unit

}

class ApiService extends API {

  var cards = SpanishCardGenerator.spanishCards
  var reviews = Seq.empty[ReviewItem]

  override def getCards(): Seq[FlashCard] = {
    cards
  }

  override def updateReview(reviewId: String, ease: Int, time:DateTime): Unit = {

    val updatedReviewItem: Option[(ReviewItem, ReviewState)] = for {
      reviewItem <- reviews.find(_.id == reviewId)
      newState: ReviewState = newReviewState(time, ease, reviewItem.due, reviewItem.reviewProgress)
    } yield (reviewItem, newState)

    updatedReviewItem match {
      case None => println("couldn't update no item found")
      case Some((item, newState)) =>
        cards.collect {
          case i if i.id == item.id => item.copy(reviewProgress = newState, due = newState.calculateDue(DateTime.now()))
          case i => i
        }
        println(s"updated $item")
    }

  }

  private def newReviewState(time: DateTime, ease: Int, due: DateTime, progress: ReviewState): ReviewState = {
    val delay: Int = Math.max(new Period(due, time).getDays, 0) // early review case

    AnkiDroidSrsAlgorithm.review(progress, delay, ease)
  }

  override def getReviews(userId: String): Seq[ReviewItem] = {
    reviews
  }

  override def addReview(factId: String): Seq[ReviewItem] = {
    reviews :+= ReviewItem(UUID.randomUUID().toString, factId, ReviewState.InitialReviewState, DateTime.now())
    reviews
  }

}



