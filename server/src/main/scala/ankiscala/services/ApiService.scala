package ankiscala.services

import java.util.UUID

import akka.util.Timeout
import ankiscala.facts.Fact
import ankiscala.services.ReviewService._
import de.nachtfische.sampledata.SpanishCards
import akka.actor._
import akka.pattern.ask
import org.joda.time.DateTime
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
      val inReview: List[String] = user.reviewItems
        .byId
        .values.map(_.factId).toList

      val ignored: List[String] = user.reviewItems
        .ignoredFacts
        .map(_._1)
        .toList

      cards.toIterable
        .filterNot(c => inReview.contains(c.id))
        .filterNot(c => ignored.contains(c.id))
        .take(20)
        .toSeq
        .map(SpanishCards.SpanishNouns.noun2card)
  }

  override def updateReview(userId:String, reviewId: String, ease: Int, time: Long): Unit =
    withUserState(userId) { updateState(_, FactReviewed(reviewId, time, ease)) }

  override def ignoreFact(userId:String, factId: String): Unit = withUserState(userId) { user =>
    updateState(user, FactIgnored(factId, nowInMillis))
  }

  private def updateState(user: UserState, event: ReviewEvent): Unit = {
    users = users.updated(user.id, user.apply(event))
  }

  override def getReviews(userId: String, until:Long): Seq[ReviewItem] = withUserState(userId) { user =>
    user.reviewItems.byId
      .values.toSeq
//      .filter(_.due < until)
      .sortBy(_.due)
  }

  override def newReview(userId: String, factId: String): Unit = withUserState(userId) { user =>
    updateState(user, FactAdded(UUID.randomUUID().toString, factId, nowInMillis))
  }

  def nowInMillis: Time = {
    new DateTime().getMillis
  }

  override def getCard(id: String): Card = {
    cards.find(_.id == id)
      .map(SpanishCards.SpanishNouns.noun2card)
      .getOrElse {
      Card("some", "some", "some")
    }
  }

  override def searchNounFact(query: String): Seq[Fact] = {

    def containedLowerCase(noun: String): Boolean = {
      noun.toLowerCase.contains(query.toLowerCase)
    }

    cards.filter(nf => {
      containedLowerCase(nf.noun) || containedLowerCase(nf.definition)
    })
  }
}
