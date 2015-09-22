package ankiscala.client

import ankiscala.client.components._
import japgolly.scalajs.react._
import org.scalajs.dom
import rx._
import rx.ops._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport


@JSExport
object AnkiScalaMain extends JSApp {

  val USER_KEY: String =  "userId"

  val userId:Var[Option[String]] = Var(dom.ext.SessionStorage(USER_KEY))

  @JSExport
  def main(): Unit = {
    userId.foreach( _ => React.render(MyRouter.Component(), dom.document.getElementById("root")))
  }

}


