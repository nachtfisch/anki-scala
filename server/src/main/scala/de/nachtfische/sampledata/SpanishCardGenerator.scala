package de.nachtfische.sampledata

import java.util.UUID

import de.nachtfische.CommonConst
import de.nachtfische.ankimodel.MustacheRenderer
import de.nachtfische.ankiscala.services.{QuestionAnswerPair, FlashCard}

import scala.io.BufferedSource

object SpanishCardGenerator {

    def spanishCards: List[FlashCard] = {
        getNouns ++
          getWords(CommonConst.PROJECT_PATH + "src/main/resources/data/verbs-top-1193.csv") ++
          getWords(CommonConst.PROJECT_PATH + "src/main/resources/data/adjectives-top1100.csv")
    }

    def getWords(s: String): List[FlashCard] = {
        parseCards(SpanishNounReader.parseOther, s)
          .map(other2FlashCard)
          .toList
    }

    def getNouns: List[FlashCard] = {
        parseCards(SpanishNounReader.parseNoun, CommonConst.PROJECT_PATH + "src/main/resources/data/spanish-nouns-top-2514.csv")
          .map(noun2FlashCard)
          .toList
    }

    def noun2FlashCard(n: Noun): FlashCard = {
        val question: String = MustacheRenderer.renderTemplate("{{word}} ({{rank}})", Map("word" -> n.noun, "rank" -> n.rank.toString))
        val answer: String = MustacheRenderer.renderTemplate("{{definition}} ({{gender}})", Map("definition" -> n.definition, "gender" -> n.gender.det))

        FlashCard(UUID.randomUUID().toString, QuestionAnswerPair(question, answer))
    }

    def other2FlashCard(ow: OtherWord): FlashCard = {
        val question: String = MustacheRenderer.renderTemplate("{{word}} ({{rank}})",
            Map("word" -> ow.verb, "rank" -> ow.rank.toString))
        val answer: String = MustacheRenderer.renderTemplate("{{definition}} ({{gender}})",
            Map("definition" -> ow.definition, "gender" -> ow.speciality.getOrElse("no speciality")))

        FlashCard(UUID.randomUUID().toString, QuestionAnswerPair(question, answer))
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
