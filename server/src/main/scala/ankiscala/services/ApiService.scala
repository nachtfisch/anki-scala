package ankiscala.services

import java.util.UUID

import akka.util.Timeout
import ankiscala.services.ReviewService._
import de.nachtfische.sampledata.SpanishCards
import akka.actor._
import akka.pattern.ask
import play.libs.Akka
import scala.concurrent.duration._
import scala.concurrent.Await


class ApiService extends API {

  var cards = SpanishCards.SpanishNouns.allCards
  var reviews = ReviewService.ReviewItems()

  val system = Akka.system
  
  private val persister: ActorRef = system.actorOf(Props(classOf[ReviewPersistenceActor], "singleUser"))
  initializeReviews()


  override def getCardSuggestions(userId:String): Seq[Card] = {

    val alreadyKnown: List[String] = reviews.byId.values.map(_.factId).toList

    cards.toIterable
      .filterNot(c => alreadyKnown.contains(c.id))
      .take(20)
      .toSeq
  }

  override def updateReview(reviewId: String, ease: Int, time: Long): Unit = {
    val reviewed: FactReviewed = FactReviewed(reviewId, time, ease)
    persister ! Persist(reviewed)

    reviews = reviews.apply(reviewed)
  }

  override def getReviews(userId: String, until:Long): Seq[ReviewItem] = {
    reviews.byId
      .values.toSeq
//      .filter(_.due < until)
      .sortBy(_.due)
  }

  override def newReview(userId: String, factId: String): Unit = {
    val added: FactAdded = FactAdded(UUID.randomUUID().toString, factId)
    persister ! Persist(added)
    reviews = reviews.apply(added)
  }

  private def initializeReviews(): Unit = {
    implicit val timout = Timeout(100 seconds)
    val events = Await.result(persister ? GetState, timout.duration).asInstanceOf[Seq[ReviewEvent]]
    reviews = ReviewItems.fromEventStream(events: _*)

  }

  override def getCard(id: String): Card = {
    cards.find(_.id == id).getOrElse {
      Card("some", "some", "some")
    }
  }
}
