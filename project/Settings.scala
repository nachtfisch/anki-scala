import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

/**
 * Application settings. Configure the build for your application here.
 * You normally don't have to touch the actual build definition after this.
 */
object Settings {
  /** The name of your application */
  val name = "scalajs-spa"

  /** The version of your application */
  val version = "1.0.2"

  /** Options for the scala compiler */
  val scalacOptions = Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  )

  /** Set some basic options when running the project with Revolver */
  val jvmRuntimeOptions = Seq(
    "-Xmx1G"
  )

  /** Declare global dependency versions here to avoid mismatches in multi part dependencies */
  object versions {
    val scala = "2.11.7"

    val akka = "2.3.13"
    val spray = "1.3.3"
    val json4s = "3.2.11"

    val scalaDom = "0.8.1"
    val scalajsReact = "0.9.1"
    val scalaCSS = "0.3.0"
    val scalaRx = "0.2.8"
    val log4js = "1.4.10"
    val autowire = "0.2.5"
    val booPickle = "1.1.0"
    val uTest = "0.3.1"

    val react = "0.12.2"
    val jQuery = "1.11.1"
    val bootstrap = "3.3.2"
    val chartjs = "1.0.1"

    val playScripts = "0.3.0"
  }

  /**
   * These dependencies are shared between JS and JVM projects
   * the special %%% function selects the correct version for each project
   */
  val sharedDependencies = Def.setting(Seq(
    "com.lihaoyi" %%% "autowire" % versions.autowire,
    "me.chrons" %%% "boopickle" % versions.booPickle,
    "com.lihaoyi" %%% "utest" % versions.uTest,
    "joda-time" % "joda-time" % "2.8.2"
  ))

  /** Dependencies only used by the JVM project */
  val jvmDependencies = Def.setting(Seq(
    "com.vmunier" %% "play-scalajs-scripts" % versions.playScripts,
    "org.scalaz" %% "scalaz-core" % "7.1.3",
    "org.webjars" % "font-awesome" % "4.3.0-1" % Provided,
    "org.webjars" % "bootstrap" % versions.bootstrap % Provided,
    "com.lambdaworks" %% "jacks" % "2.3.3",
    "com.typesafe.slick" %% "slick" % "3.0.0",
    "org.xerial" % "sqlite-jdbc" % "3.7.2",
    "joda-time" % "joda-time" % "2.8.2",
    "org.json4s" %% "json4s-jackson" % Settings.versions.json4s,
    "org.json4s" %% "json4s-ext" % Settings.versions.json4s,
    "com.github.spullara.mustache.java" % "compiler" % "0.8.17",
    "org.elasticsearch" % "elasticsearch" % "1.7.1",
    "io.spray" %% "spray-can" % Settings.versions.spray,
    "io.spray" %% "spray-routing" % Settings.versions.spray,
    "com.typesafe.akka" %% "akka-actor" % Settings.versions.akka,
    "com.typesafe.akka" %% "akka-persistence-experimental" % Settings.versions.akka,
    "com.geteventstore" %% "akka-persistence-eventstore" % "1.1.0"
//    "io.spray" %% "spray-testkit" % Settings.versions.spray % "test",
//    "com.typesafe.akka" %% "akka-testkit" % Settings.versions.akka % "test",
//    "org.specs2" %% "specs2-core" % "2.3.11" % "test"
  ))

  /** Dependencies only used by the JS project (note the use of %%% instead of %%) */
  val scalajsDependencies = Def.setting(Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % versions.scalajsReact,
    "com.github.japgolly.scalajs-react" %%% "extra" % versions.scalajsReact,
    "com.github.japgolly.scalacss" %%% "ext-react" % versions.scalaCSS,
    "org.scala-js" %%% "scalajs-dom" % versions.scalaDom,
    "com.lihaoyi" %%% "scalarx" % versions.scalaRx
  ))

  /** Dependencies for external JS libs that are bundled into a single .js file according to dependency order */
  val jsDependencies = Def.setting(Seq(
    "org.webjars" % "react" % versions.react / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
    "org.webjars" % "jquery" % versions.jQuery / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "bootstrap" % versions.bootstrap / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js",
    "org.webjars" % "chartjs" % versions.chartjs / "Chart.js" minified "Chart.min.js",
    "org.webjars" % "log4javascript" % versions.log4js / "js/log4javascript_uncompressed.js" minified "js/log4javascript.js"
  ))
}