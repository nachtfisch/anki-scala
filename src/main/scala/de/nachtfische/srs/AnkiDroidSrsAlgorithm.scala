package de.nachtfische.srs

import org.joda.time.{Period, DateTime}

case class ReviewState(factor: Double, level: Int) {
    def calculateDue(fromDate: DateTime): DateTime = {
        fromDate.plus(Period.days(level))
    }
}

object ReviewState {
    val InitialReviewState = ReviewState(2.5, 1)
    val EasyStart = ReviewState(2.5, 4)
}

object AnkiDroidSrsAlgorithm {

    val FACTOR_ADDITION_VALUES: List[Double] = List(-0.150, 0, 0.150)

    def nextInterval(delay: Int, factor: Double, currentInterval: Int, ease: Int): Int = {
        // config
        val optionalIntervalFactor: Double = 1.0
        val veryEasyBoost: Double = 1.3
        val maxInterval: Int = 36500

        val maxWithIncrementSnd = constrainedInterval(optionalIntervalFactor, _: Int, _: Double)
        // next interval calculation
        val intervalHard = maxWithIncrementSnd(((currentInterval + delay / 4) * 1.2).toInt, currentInterval)
        val intervalNormal = maxWithIncrementSnd(((currentInterval + delay / 2) * factor).toInt, intervalHard)
        val intervalEasy = maxWithIncrementSnd(((currentInterval + delay) * factor * veryEasyBoost).toInt, intervalNormal)

        var interval: Int = 0
        if (ease == 2) {
            interval = intervalHard
        } else if (ease == 3) {
            interval = intervalNormal
        } else {
            interval = intervalEasy
        }

        // constraint
        Math.min(interval, maxInterval)
    }

    def constrainedInterval(optionalLevelFactor: Double, ivl: Int, prev: Double): Int = {
        Math.max(ivl * optionalLevelFactor, prev + 1).toInt
    }

    def newFactor(oldFactor: Double, ease: Int): Double = {
        Math.max(1.3, oldFactor + FACTOR_ADDITION_VALUES(ease - 2))
    }

    def review(old: ReviewState, delay: Int, ease: Int): ReviewState = {
        ReviewState(newFactor(old.factor, ease), nextInterval(delay, old.factor, old.level, ease))
    }

    /* convenience method to do multi review */
    final def multiReview(state: ReviewState, eases: List[Int]): List[ReviewState] = eases match {
        case Nil => List()
        case ease :: rest =>
            val updatedState = review(state, 1, ease)
            updatedState :: multiReview(updatedState, rest)
    }

}
