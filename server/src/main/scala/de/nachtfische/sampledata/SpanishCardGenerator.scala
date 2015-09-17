package de.nachtfische.sampledata

import java.util.UUID

import ankiscala.services.Card
import de.nachtfische.CommonConst
import de.nachtfische.ankimodel.MustacheRenderer

import scala.io.BufferedSource

object SpanishCardGenerator {

    def spanishCards: List[Card] = {
        getNouns ++
          getWords(CommonConst.PROJECT_PATH + "src/main/resources/data/verbs-top-1193.csv") ++
          getWords(CommonConst.PROJECT_PATH + "src/main/resources/data/adjectives-top1100.csv")
    }

    def getWords(s: String): List[Card] = {
        parseCards(SpanishNounReader.parseOther, s)
          .map(other2FlashCard)
          .toList
    }

    def getNouns: List[Card] = {
        parseCards(SpanishNounReader.parseNoun, CommonConst.PROJECT_PATH + "src/main/resources/data/spanish-nouns-top-2514.csv")
          .map(noun2FlashCard)
          .toList
    }

    def noun2FlashCard(n: Noun): Card = {
        val question: String = MustacheRenderer.renderTemplate("{{word}} ({{rank}})", Map("word" -> n.noun, "rank" -> n.rank.toString))
        val answer: String = MustacheRenderer.renderTemplate("{{definition}} ({{gender}})", Map("definition" -> n.definition, "gender" -> n.gender.det))

        Card(UUID.randomUUID().toString, question, answer)
    }

    def other2FlashCard(ow: OtherWord): Card = {
        val question: String = MustacheRenderer.renderTemplate("{{word}} ({{rank}})",
            Map("word" -> ow.verb, "rank" -> ow.rank.toString))
        val answer: String = MustacheRenderer.renderTemplate("{{definition}} ({{gender}})",
            Map("definition" -> ow.definition, "gender" -> ow.speciality.getOrElse("no speciality")))

        Card(UUID.randomUUID().toString, question, answer)
    }

    def parseCards[T](parser: (String) => Either[String, T], path: String): Iterator[T] = {
        getSource(path).getLines()
          .map(parser)
          .flatMap(t => t.right.toOption)
    }

    def getSource(s: String): BufferedSource = {
        scala.io.Source.fromFile(s)
    }

}
