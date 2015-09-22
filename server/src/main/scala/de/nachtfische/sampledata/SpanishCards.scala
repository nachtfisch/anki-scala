package de.nachtfische.sampledata

import java.util.UUID

import ankiscala.services.Card
import de.nachtfische.CommonConst
import de.nachtfische.ankimodel.MustacheRenderer

import scala.io.Source
import scalaz.Scalaz._

object SpanishCards {

    def spanishCards: List[Card] = {
        SpanishNouns.allCards ++
         SpanishVerbsAndAdjectives.allCards
    }

    trait CardProvider {
        def allCards:List[Card]
    }

    object SpanishVerbsAndAdjectives extends CardProvider {
        override def allCards: List[Card] = {
            getWords(CommonConst.PROJECT_PATH + "src/main/resources/data/verbs-top-1193.csv") ++
              getWords(CommonConst.PROJECT_PATH + "src/main/resources/data/adjectives-top1100.csv")
        }

        def getWords(s: String): List[Card] = Source.fromFile(s)
              .getLines()
              .drop(1) // remove header
              .map(parseOther)
              .flatMap(t => t.right.toOption)
              .map(other2FlashCard)
              .toList

        // encender (e-ie) (2133)$$$to turn on
        def parseOther(stringToParse:String):Either[String,OtherWord] = {
            val wordFieldInfo = "^(.*)(\\(.*\\))?\\((\\d+)\\)$".r
            val split: List[String] = stringToParse.split("\\$\\$\\$").toList
            val fieldInfo = wordFieldInfo.findFirstMatchIn(split(0)).map( t => (t.group(1), Option(t.group(2)), t.group(3)))

            fieldInfo.map(f => OtherWord(f._1.trim, split(1),f._3.toInt, f._2.map(_.trim))).toRight(stringToParse)
        }


        def other2FlashCard(ow: OtherWord): Card = {
            val question: String = MustacheRenderer.renderTemplate("{{word}}",
                Map("word" -> ow.verb, "rank" -> ow.rank.toString))
            val answer: String = MustacheRenderer.renderTemplate("{{definition}} ({{gender}})",
                Map("definition" -> ow.definition, "gender" -> ow.speciality.getOrElse("no speciality")))

            Card(UUID.randomUUID().toString, question, answer)
        }

    }

    object SpanishNouns extends CardProvider {

        case class NounFact(id:String, gender: Gender, noun: String, definition: String, rank: Int)
        case class Gender(det:String)

        private val resourcePath: String = CommonConst.PROJECT_PATH + "src/main/resources/data/spanish-nouns-keyd.csv"

        override def allCards: List[Card] = Source.fromFile(resourcePath)
              .getLines()
              .drop(1) // remove header
              .map(parseNoun map noun2card)
              .toList

        // example-format: n-spa-1872;;el;;cumpleaños;;birthday;;3785
        def parseNoun: String => NounFact = { line =>
            val split: List[String] = line.split(";;").toList
            assert(split.size == 5) // right format

            NounFact(split(0), parseGender(split(1)), split(2), split(3), split(4).toInt)
        }

        def parseGender(genderString: String): Gender = genderString match {
            case "el" => Gender("el")
            case "la" => Gender("la")
            case "los" => Gender("los")
            case "las" => Gender("las")
            case _ => throw new IllegalArgumentException("couldn't parse gender " + genderString)
        }

        def noun2card(n: NounFact): Card = {

            val answer: String = MustacheRenderer.renderTemplate("{{definition}}", Map("definition" -> n.definition, "gender" -> n.gender.det))
            Card(n.id, renderQuestion(n), answer)
        }

        def renderQuestion(n: NounFact) = MustacheRenderer.renderTemplate(
            "{{gender}} {{word}}", Map("word" -> n.noun, "rank" -> n.rank.toString, "gender" -> n.gender.det))

    }

}
