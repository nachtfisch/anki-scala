package de.nachtfische.web

import de.nachtfische.ankimodel.CardRendering
import CardRendering.RenderRequest
import spray.httpx.Json4sJacksonSupport
import spray.routing.{HttpServiceBase, MalformedRequestContentRejection}



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
