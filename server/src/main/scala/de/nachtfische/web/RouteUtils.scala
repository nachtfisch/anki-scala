package de.nachtfische.web
import spray.http.MediaTypes
import spray.routing.directives.{RespondWithDirectives, RouteDirectives}

trait RouteUtils {
    this: RespondWithDirectives with RouteDirectives =>

    val completeWithJson = respondWithMediaType(MediaTypes.`application/json`) & complete
}
