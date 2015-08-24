package ankiscala.services


case class ReviewState(factor: Double, level: Int)
case class ReviewItem(id:String, factId:String, reviewProgress:ReviewState, due:Long)
case class ReviewRequest(ease:Int, reviewTime:Long)

object ReviewState {
  val InitialReviewState = ReviewState(2.5, 1)
  val EasyStart = ReviewState(2.5, 4)
}


