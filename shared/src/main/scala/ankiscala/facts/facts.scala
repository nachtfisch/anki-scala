package ankiscala.facts

sealed trait Fact

case class Gender(det:String)
case class NounFact(id:String, gender: Gender, noun: String, definition: String, rank: Int) extends Fact


