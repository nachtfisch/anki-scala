package de.nachtfische.web

import de.nachtfische.web.CardRendering.RenderRequest
import spray.httpx.Json4sJacksonSupport
import spray.routing.{HttpServiceBase, MalformedRequestContentRejection}

object CardRendering {
    case class QuestionAnswerPair(question: String, answer: String)
    case class Template(front: String, back: String)
    case class ModelField(name: String, value: String)
    case class ModelFact(fields: List[ModelField])
    case class RenderRequest(templates: List[Template], facts: List[ModelFact])
}

trait CardRoutes extends HttpServiceBase with Json4sJacksonSupport with RouteUtils {

    val cardRoutes = path("api" / "cards" / "render") {
        post {
            entity(as[RenderRequest]) { renderRequest =>
                if (renderRequest.facts.isEmpty || renderRequest.templates.isEmpty) {
                    reject(MalformedRequestContentRejection("can not be empty"))
                } else {
                    completeWithJson {
                        Map[String, String]("id" -> "successfull")
                    }
                }
            }
        }
    }
}
