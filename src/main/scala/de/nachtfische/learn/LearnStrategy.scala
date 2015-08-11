package de.nachtfische.learn

import scala.concurrent.duration._

case class LearnState(stage: Int)
object InitialLearnState extends LearnState(0)

sealed trait LearnResult
case object Complete extends LearnResult
case class Reschedule(time: Duration, newProgress: LearnState) extends LearnResult

sealed trait Answer
case object Again extends Answer
case object Understood extends Answer
case object Learned extends Answer

class RepeatNTimesWithTimeIntervalLearnStrategy(numberOfStages: Int) {

    def learn(progress: LearnState, answer: Answer): LearnResult = answer match {
        case Again => Reschedule(1.minutes, InitialLearnState)
        case Understood if progress.stage < numberOfStages => Reschedule(10.minutes, LearnState(progress.stage + 1))
        case Understood => Complete
        case Learned => Complete
    }

}

object RepeatTwiceLearnStrategy extends RepeatNTimesWithTimeIntervalLearnStrategy(2)