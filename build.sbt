name := """trello-service"""
organization := "com.nashtech"
version := "1.0-SNAPSHOT"

// Dependency versions
val playSlickVersion = "6.1.1"
val postgresVersion = "42.7.3"
val jacksonVersion = "2.14.3"

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    .settings (
      scalaVersion := "2.13.16",

      libraryDependencies ++= Seq(
        guice,
        caffeine,
        filters,

        "org.playframework" %% "play-slick" % playSlickVersion,
        "org.playframework" %% "play-slick-evolutions" % playSlickVersion,
        "org.postgresql" % "postgresql" % postgresVersion,
        "com.github.t3hnar" % "scala-bcrypt_2.13" % "4.3.0",

        "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
        "org.mockito" %% "mockito-scala-scalatest" % "1.17.29" % Test,
        "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
        "com.h2database" % "h2" % "2.2.224" % Test,

        // Explicitly add compatible Jackson versions
        "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
        "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
        "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % jacksonVersion,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion,

        //  JWT
        "com.auth0" % "java-jwt" % "4.5.0",
        // Environment variable loading
        "io.github.cdimascio" % "dotenv-java" % "3.2.0",
        "com.typesafe.play" %% "play-ws" % "2.9.8",
        "com.typesafe.play" %% "play-json" % "2.10.7",
        "org.jsoup" % "jsoup" % "1.21.1",
        "org.playframework" %% "play-ahc-ws" % "3.0.8"
      ),

      // CRITICAL: Force Jackson versions to prevent conflicts
      dependencyOverrides ++= Seq(
        "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
        "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
        "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % jacksonVersion,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion
      ),

      coverageExcludedFiles := ".*ReverseRoutes.scala",
      coverageEnabled     := true,
      coverageMinimumStmtTotal := 80,
      coverageFailOnMinimum := false,
      coverageHighlighting := true,
        coverageExcludedPackages := Seq(
            "controllers\\.javascript\\..*",
            "controllers.ReverseAuthController",
            "controllers.ReverseHomeController",
            "dto\\.request\\.auth\\..*",
            "dto\\.response\\..*",
            "filters\\..*",
            "models\\.tables\\..*",
            "modules\\..*",
            "router\\..*",
            "init\\..*",
            "exception\\..*",
        ).mkString(";")

    )
