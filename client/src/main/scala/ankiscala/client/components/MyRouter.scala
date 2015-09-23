package ankiscala.client.components

import ankiscala.client.AnkiScalaMain
import ankiscala.client.components.LearningModule.{Backend, Props}
import ankiscala.client.services.{LearnCardsStore, ReviewStore}
import japgolly.scalajs.react.{ReactComponentM_, BackendScope, ReactComponentB, ReactElement}
import japgolly.scalajs.react.extra.router2._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.Ref
import org.scalajs.dom.raw.HTMLInputElement
import rx.core.{Rx, Var}

import scala.scalajs.js.UndefOr

sealed trait Pages
case object SearchCardsPage extends Pages
case object LearningPage extends Pages
case object ReviewPage extends Pages
case object LoginPage extends Pages


object MyRouter {

  private val baseUrl = BaseUrl.fromWindowOrigin_/
  
  val Component = Router(baseUrl, routerConfig.logToConsole)

  private def routerConfig = RouterConfigDsl[Pages].buildConfig { dsl =>
    import dsl._
    import japgolly.scalajs.react.extra.router2.StaticDsl.Rule

    val privateRoute: Rule[Pages] = (emptyRule
      | staticRoute(root, SearchCardsPage) ~> renderR(rt => CardModule.CardComponent(rt))
      | staticRoute("#review", ReviewPage) ~> renderR(rt => ReviewModule.Component(ReviewModule.Props(ReviewStore.reviewList, rt)))
      | staticRoute("#card", LearningPage) ~> renderR(rt =>
          LearningModule.Component(LearningModule.Props(Rx{ LearnCardsStore.availableCards().headOption }, rt)))
      )
      .addCondition(isUserLoggedIn)(_ => Some(redirectToPage(LoginPage)(Redirect.Replace)))


    val loginRoute: RouterConfigDsl[Pages]#Rule = staticRoute("#login", LoginPage) ~> renderR( rt => Authentication.LoginView(rt))
    ( loginRoute
      | privateRoute
      )
      .notFound(redirectToPage(SearchCardsPage)(Redirect.Replace))
      .renderWith(layoutWithMainMenu)

  }

  def isUserLoggedIn = AnkiScalaMain.userId().isDefined


  private def layoutWithMainMenu: (RouterCtl[Pages], Resolution[Pages]) => ReactElement = { (router, content) =>
    <.div(
      <.div(MainMenu.Component(router)),
      content.render()
    )
  }


  object MainMenu {
    val menuPages = Seq(SearchCardsPage, LearningPage, ReviewPage)

    case class MenuProps(router:RouterCtl[Pages], user:Option[String])

    val Component = ReactComponentB[RouterCtl[Pages]]("Menu")
      .render((props) => <.div(
        <.ul(menuPages map { t => <.li(props.link(t)(t.getClass.getName)) }),
        loginInfo,
        Authentication.LogoutButton()
        )
      ).build

    def loginInfo = AnkiScalaMain.userId() match {
      case Some(user) => "logged in as '" + user + "'"
      case None => "not logged in"
    }

  }

}
