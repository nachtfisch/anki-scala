package de.nachtfische.sampledata

import java.util.UUID

import ankiscala.facts.{Fact, Gender, NounFact}
import ankiscala.services.Card
import de.nachtfische.CommonConst
import de.nachtfische.ankimodel.MustacheRenderer

import scala.io.Source

object SpanishCards {

    case class OtherWord(verb:String, definition:String, rank:Int, speciality:Option[String])

    object SpanishVerbsAndAdjectives  {
        def allCards: List[OtherWord] = {
            getWords(CommonConst.PROJECT_PATH + "src/main/resources/data/verbs-top-1193.csv") ++
              getWords(CommonConst.PROJECT_PATH + "src/main/resources/data/adjectives-top1100.csv")
        }

        def getWords(s: String): List[OtherWord] = Source.fromFile(s)
              .getLines()
              .drop(1) // remove header
              .map(parseOther)
              .flatMap(t => t.right.toOption)
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

    object SpanishNouns {


        private val resourcePath: String = CommonConst.PROJECT_PATH + "src/main/resources/data/spanish-nouns-keyd.csv"

        def allCards: List[NounFact] = Source.fromFile(resourcePath)
              .getLines()
              .drop(1) // remove header
              .map(parseNoun)
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
