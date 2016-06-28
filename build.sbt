name := """lite-permission-finder"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava).dependsOn(`zzz-common`)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)

// Contains all files and libraries shared across other projects
lazy val `zzz-common` = project.in(file("subprojects/lite-play-common")).enablePlugins(PlayJava)