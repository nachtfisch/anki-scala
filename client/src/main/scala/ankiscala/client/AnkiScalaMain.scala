package ankiscala.client

import ankiscala.client.components._
import ankiscala.client.services.LearnCardsStore
import japgolly.scalajs.react._
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport


@JSExport
object AnkiScalaMain extends JSApp {

  @JSExport
  def main(): Unit = {
    LearnCardsStore.refreshAvailableCards()

    React.render(MyRouter.Component(), dom.document.getElementById("root"))
  }

}


