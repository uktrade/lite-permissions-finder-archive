name := """lite-permissions-finder"""

version := scala.util.Properties.envOrElse("BUILD_VERSION", "1.0-SNAPSHOT")

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .dependsOn(`zzz-common` % "test->test;compile->compile")
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "buildinfo"
  )

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  javaJdbc,
  cacheApi,
  javaWs,
  filters
)

libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.2"

libraryDependencies += "org.flywaydb" %% "flyway-play" % "5.0.0"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.2"
libraryDependencies += "org.jdbi" % "jdbi" % "2.78"

libraryDependencies += "org.apache.poi" % "poi" % "3.17"
libraryDependencies += "org.apache.poi" % "poi-ooxml" % "3.17"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.7"

libraryDependencies += "uk.gov.bis.lite" % "lite-search-management-api" % "1.1"
libraryDependencies += "uk.gov.bis.lite" % "lite-control-code-service-api" % "1.3"

libraryDependencies += "au.com.dius" % "pact-jvm-provider-junit_2.11" % "3.5.13" % "test"
libraryDependencies += "au.com.dius" % "pact-jvm-consumer-junit_2.11" % "3.5.13" % "test"

resolvers += "Lite Lib Releases " at "https://nexus.ci.uktrade.io/repository/maven-releases/"
resolvers += Resolver.sonatypeRepo("releases")

// Contains all files and libraries shared across other projects
lazy val `zzz-common` = project.in(file("subprojects/lite-play-common")).enablePlugins(PlayJava)

//Add build time and git commit to the build info object

buildInfoKeys ++= Seq[BuildInfoKey](
  BuildInfoKey.action("gitCommit") {
    try {
      "git rev-parse HEAD".!!.trim
    } catch {
      case e: Throwable => "unknown (" + e.getMessage + ")"
    }
  }
)

buildInfoOptions += BuildInfoOption.BuildTime
buildInfoOptions += BuildInfoOption.ToJson

// Production only pipeline stages, (after the above production and dev pipeline stages)
pipelineStages := Seq(digest)
