import sbt.Test

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.4"

lazy val root = (project in file("."))
  .settings(
    name := "PPS-25-functus",
    idePackagePrefix := Some("org.pps.functus"),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % Test,
    libraryDependencies += "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0" % Test,
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    libraryDependencies += "org.jline" % "jline" % "4.4.2",

    // disable parallel execution for view testing,
    // otherwise the standard output is mixed and the tests will fail
    Test / parallelExecution := false,
    // run test on the same JVM for coverage report
    Test / fork := true,

    //excluding main from coverage test
    coverageExcludedPackages := "org\\.pps\\.functus\\.Main",

    // specify the Main to be executed at launch of the .jar file
    assembly / mainClass := Some("org.pps.functus.Main"),
    // Name of the generated .jar file
    assembly / assemblyJarName := "Functus.jar"

  )
