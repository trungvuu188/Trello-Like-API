name := """trello-service"""
organization := "com.nashtech"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

// Dependency versions
val playSlickVersion = "6.1.1"
val postgresVersion = "42.7.3"

libraryDependencies ++= Seq(
  guice,
  "org.playframework" %% "play-slick" % playSlickVersion,
  "org.playframework" %% "play-slick-evolutions" % playSlickVersion,
  "org.postgresql" % "postgresql" % postgresVersion,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  "com.github.t3hnar" % "scala-bcrypt_2.13" % "4.3.0",
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.29" % Test,
)

