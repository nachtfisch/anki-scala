package ankiscala.services

trait API {

  def getCards(): Seq[FlashCard]

  def getReviews(userId:String): Seq[ReviewItem]

  def addReview(factId:String): Seq[ReviewItem]

  def updateReview(reviewId: String, ease: Int, time:Long): Unit

}





