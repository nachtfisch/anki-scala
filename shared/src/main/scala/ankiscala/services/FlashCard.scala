package ankiscala.services

case class QuestionAnswerPair(question: String, answer: String)

case class FlashCard(id:String, questionAnswerPair: QuestionAnswerPair)