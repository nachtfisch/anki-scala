package ankiscala.services

trait API {

  def getCardSuggestions(userId:String): Seq[Card]

  def getCard(id:String):Card

  def getReviews(userId:String, until:Long): Seq[ReviewItem]

  def newReview(factId:String): Unit

  def updateReview(reviewId: String, ease: Int, time:Long): Unit

}





