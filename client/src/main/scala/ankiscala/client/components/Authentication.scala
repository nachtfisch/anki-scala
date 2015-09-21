package ankiscala.client.components

import ankiscala.client.AnkiScalaMain
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, ReactComponentM_, BackendScope, Ref}
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.dom


import scala.scalajs.js.UndefOr

object Authentication {

    val theInput = Ref[HTMLInputElement]("theInput")

    class Backend(backend: BackendScope[_, _]) {
        def setUserId() = {

            val input: UndefOr[ReactComponentM_[HTMLInputElement]] = theInput(backend)

            if (input.isDefined) {
                val value: String = input.get.getDOMNode().value
                dom.sessionStorage.setItem(AnkiScalaMain.USER_KEY, value)
                val someString: Option[String] = Some(value)
                AnkiScalaMain.userId() = someString
            } else {
                println("ref not defined")
            }
        }

    }

    val LoginView = ReactComponentB[RouterCtl[Pages]]("Login")
      .stateless
      .backend(new Backend(_))
      .render((_, _, backend) =>
        <.div("what's your name"
            , <.input(^.ref := theInput), <.button("login", ^.onClick --> {
                backend.setUserId()
            })))
      .build

    object LogoutBackend {
        def logout() = {
            dom.sessionStorage.removeItem(AnkiScalaMain.USER_KEY)
            AnkiScalaMain.userId() = None

        }
    }

    val LogoutButton = ReactComponentB[Unit]("LogoutButton")
      .stateless
      .backend(_ => LogoutBackend)
      .render((_, _, backend) => <.button("logout", ^.onClick --> backend.logout))
      .buildU

}

