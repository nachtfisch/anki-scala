package de.nachtfische.web

import spray.routing.HttpServiceBase


object StaticRoutes extends HttpServiceBase {
    val routes = path("app") {
        getFromResource("webapp/index.html")
    } ~ pathPrefix("app" / "js") {
        getFromResourceDirectory("webapp/js")
    }
}
