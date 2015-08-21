
organization := "com.github.nachtfisch"

name := "anki-scala"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  val json4sV: String = "3.2.11"

  Seq(
    "com.lambdaworks" %% "jacks" % "2.3.3",
    "com.typesafe.slick" %% "slick" % "3.0.0",
    "org.xerial" % "sqlite-jdbc" % "3.7.2",
    "joda-time" % "joda-time" % "2.8.2",
    "org.json4s" %% "json4s-jackson" % json4sV,
    "org.json4s" %% "json4s-ext" % json4sV,
    "com.github.spullara.mustache.java" % "compiler" % "0.8.17",
    "org.elasticsearch" % "elasticsearch" % "1.7.1",
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

Revolver.settings: Seq[sbt.Def.Setting[_]]