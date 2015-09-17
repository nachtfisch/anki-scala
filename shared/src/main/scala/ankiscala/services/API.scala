package ankiscala.services

trait API {

  def getCards(): Seq[FlashCard]

  def getReviews(userId:String, until:Long): Seq[ReviewItem]

  def newReview(factId:String): Unit

  def updateReview(reviewId: String, ease: Int, time:Long): Unit

}





