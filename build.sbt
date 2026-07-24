ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.4"

lazy val root = (project in file("."))
  .settings(
    name := "PPS-25-functus",
    idePackagePrefix := Some("org.pps.functus"),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % Test,
    libraryDependencies += "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0" % Test,
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    libraryDependencies += "org.jline" % "jline" % "4.3.1",

    // Specifica il Main da eseguire all'avvio del .jar
    assembly / mainClass := Some("main"),
    // Nome del file jar generato
    assembly / assemblyJarName := "Functus.jar"
  )
