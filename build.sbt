import sbt.Keys._
import sbt.Project.projectToRef

organization := "com.github.nachtfisch"

name := "anki-scala"

version := "0.1.0-SNAPSHOT"

scalaVersion := Settings.versions.scala

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

lazy val scalajvmSettings = Seq(
  scalajsOutputDir := (classDirectory in Compile).value / "public" / "javascripts"
) ++ (
  Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
    crossTarget in(client, Compile, packageJSKey) := scalajsOutputDir.value
  })


lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "com.lihaoyi" %%% "scalatags" % "0.5.2"
    )
    // by default we do development build, no eliding
//    elideOptions := Seq(),
//    scalacOptions ++= elideOptions.value,
//    jsDependencies ++= Settings.jsDependencies.value,
    // RuntimeDOM is needed for tests
//    jsDependencies += RuntimeDOM % "test",
    // yes, we want to package JS dependencies
//    skip in packageJSDependencies := false,
    // use Scala.js provided launcher code to start the client app
//    persistLauncher := true,
//    persistLauncher in Test := false,
    // must specify source maps location because we use pure CrossProject
//    sourceMapsDirectories += sharedJS.base / "..",
    // use uTest framework for tests
//    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .enablePlugins(ScalaJSPlugin)
//  .dependsOn(sharedJS)

lazy val server = (project in file("server"))
  .settings(scalajvmSettings: _*)
  .settings(
    name := "server",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= {
      Seq(
        "com.lambdaworks" %% "jacks" % "2.3.3",
        "com.typesafe.slick" %% "slick" % "3.0.0",
        "org.xerial" % "sqlite-jdbc" % "3.7.2",
        "joda-time" % "joda-time" % "2.8.2",
        "org.json4s" %% "json4s-jackson" % Settings.versions.json4s,
        "org.json4s" %% "json4s-ext" % Settings.versions.json4s,
        "com.github.spullara.mustache.java" % "compiler" % "0.8.17",
        "org.elasticsearch" % "elasticsearch" % "1.7.1",
        "io.spray"            %%  "spray-can"     % Settings.versions.spray,
        "io.spray"            %%  "spray-routing" % Settings.versions.spray,
        "io.spray"            %%  "spray-testkit" % Settings.versions.spray  % "test",
        "com.typesafe.akka"   %%  "akka-actor"    % Settings.versions.akka,
        "com.typesafe.akka"   %%  "akka-testkit"  % Settings.versions.akka  % "test",
        "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
      )
    }
//    commands += ReleaseCmd,
    // connect to the client project
//    scalaJSProjects := clients,
//    pipelineStages := Seq(scalaJSProd),
    // compress CSS
//    LessKeys.compress in Assets := true
  )
.aggregate(client)
//  .aggregate(clients.map(projectToRef): _*)
//  .dependsOn(sharedJVM)


Revolver.settings: Seq[sbt.Def.Setting[_]]