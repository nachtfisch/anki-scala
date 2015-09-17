package ankiscala.services

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import ankiscala.services.ReviewService.ReviewEvent


case class Persist(reviewEvent: ReviewEvent)
case object GetState

class ReviewPersistenceActor(id:String) extends PersistentActor with ActorLogging {

    private var eventState = Seq.empty[ReviewEvent]

    override def persistenceId: String = id

    override def receiveRecover: Receive = {
        case ev:ReviewEvent => eventState :+= ev
    }

    override def receiveCommand: Receive = {
        case Persist(ev) => persist(ev) { event =>
            log.info("persisted event" + ev.toString)
        }
        case GetState => sender() ! eventState
    }
    
}
