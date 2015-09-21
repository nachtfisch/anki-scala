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

  case class UserState(id:String, reviewItems: ReviewItems, actorRef: ActorRef) {
    def apply(event: ReviewEvent) = {
      actorRef ! Persist(event)
      copy(reviewItems = reviewItems.apply(event))
    }

  }
  
  var cards = SpanishCards.SpanishNouns.allCards
  
  val system = Akka.system
  
  var users = Map.empty[String, UserState]

  def withUserState[T](userid: ReviewId)(f:UserState => T):T = {
    users.get(userid) match {
      case Some(state) => println(state); f(state)
      case None => {
        implicit val timout = Timeout(100 seconds)
        val persister: ActorRef = newPersister(userid)
        val events = Await.result(persister ? GetState, timout.duration).asInstanceOf[Seq[ReviewEvent]]
        val stream: ReviewItems = ReviewItems.fromEventStream(events: _*)

        val state: UserState = UserState(userid, stream, persister)
        users = users.updated(userid, state)
        f(state)
      }
    }
  }

  private def newPersister(userId: String): ActorRef = {
    system.actorOf(Props(classOf[ReviewPersistenceActor], userId))
  }

  override def getCardSuggestions(userId:String): Seq[Card] = withUserState(userId) { user =>
      val alreadyKnown: List[String] = user.reviewItems
        .byId
        .values.map(_.factId).toList

      cards.toIterable
        .filterNot(c => alreadyKnown.contains(c.id))
        .take(20)
        .toSeq
  }

  override def updateReview(userId:String, reviewId: String, ease: Int, time: Long): Unit = withUserState(userId) { user =>
    val event: FactReviewed = FactReviewed(reviewId, time, ease)
    
    users = users.updated(userId, user.apply(event))
  }

  override def getReviews(userId: String, until:Long): Seq[ReviewItem] = withUserState(userId) { user =>
    user.reviewItems.byId
      .values.toSeq
//      .filter(_.due < until)
      .sortBy(_.due)
  }

  override def newReview(userId: String, factId: String): Unit = withUserState(userId) { user =>
    val event: FactAdded = FactAdded(UUID.randomUUID().toString, factId)
    users = users.updated(userId, user.apply(event))

  }

  override def getCard(id: String): Card = {
    cards.find(_.id == id).getOrElse {
      Card("some", "some", "some")
    }
  }
}
