package de.nachtfische.ankimodel

import java.io.{Writer, StringWriter, StringReader}

import com.github.mustachejava.DefaultMustacheFactory

import scala.collection.JavaConversions


object MustacheRenderer {

    def renderTemplate(template: String, context: Map[String, String]): String = {

        val mustacheFactory = new DefaultMustacheFactory() {
            override def encode(value: String, writer: Writer): Unit = writer.append(value)
        }

        val compiledTemplate = mustacheFactory.compile(new StringReader(template), "name")

        val output = new StringWriter()
        compiledTemplate.execute(output, JavaConversions.mapAsJavaMap(context))
        output.flush()

        output.toString
    }

    def renderFact(fact: Fact): List[(String, String)] = {
        val fieldMap = ((fact.modelId.flds map { f => f.name }) zip fact.fields).toMap


        fact.modelId.tmpls map { t =>
            val renderedQuestion = renderTemplate(t.qfmt, fieldMap)
            val renderedAnswer = renderTemplate(t.afmt,
                fieldMap + ("FrontSide" -> renderedQuestion))
            renderedQuestion -> renderedAnswer
        }
    }

}
