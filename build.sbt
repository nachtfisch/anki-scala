organization := "com.github.nachtfisch"

name := "anki-scala"

version := "0.1.0-SNAPSHOT"

scalaVersion := Settings.versions.scala

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"


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

Revolver.settings: Seq[sbt.Def.Setting[_]]