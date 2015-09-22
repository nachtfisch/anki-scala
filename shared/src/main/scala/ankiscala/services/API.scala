package ankiscala.services

trait API {

  def getCardSuggestions(userId:String): Seq[Card]

  def getCard(id:String):Card

  def getReviews(userId:String, until:Long): Seq[ReviewItem]

  def newReview(userId: String = "", factId: String): Unit

  def updateReview(userId:String, reviewId: String, ease: Int, time:Long): Unit

  def ignoreFact(userId:String, factId: String): Unit

}





