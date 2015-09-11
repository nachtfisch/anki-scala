package ankiscala.client.components

import ankiscala.client.services.ReviewStore
import japgolly.scalajs.react.{ReactComponentB, ReactElement}
import japgolly.scalajs.react.extra.router2._
import japgolly.scalajs.react.vdom.prefix_<^._

sealed trait Pages
case object SearchCardsPage extends Pages
case object LearningPage extends Pages
case object ReviewPage extends Pages


object MyRouter {

  private val baseUrl = BaseUrl.fromWindowOrigin_/
  
  val Component = Router(baseUrl, routerConfig.logToConsole)

  private def routerConfig = RouterConfigDsl[Pages].buildConfig { dsl =>
    import dsl._

    (emptyRule
      | staticRoute(root, SearchCardsPage) ~> renderR(rt => CardModule.CardComponent(rt))
      | staticRoute("#review", ReviewPage) ~> renderR(rt => ReviewModule.Component(ReviewModule.Props(ReviewStore.reviewList, rt)))
      | staticRoute("#card", LearningPage) ~> renderR(rt => LearningModule.Component(rt))
      )
      .notFound(redirectToPage(SearchCardsPage)(Redirect.Replace))
      .renderWith(layoutWithMainMenu)
  }

  private def layoutWithMainMenu: (RouterCtl[Pages], Resolution[Pages]) => ReactElement = { (router, content) =>
    <.div(
      <.div(MainMenu.Component(router)),
      content.render()
    )
  }


  object MainMenu {
    val menuPages = Seq(SearchCardsPage, LearningPage, ReviewPage)
    val Component = ReactComponentB[RouterCtl[Pages]]("Menu")
      .render((props) => <.div(<.ul( menuPages map {t => <.li(props.link(t)(t.getClass.getName))})))
      .build
  }

}
