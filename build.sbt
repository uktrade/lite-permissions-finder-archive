import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import net.ground5hark.sbt.concat.Import.Concat

name := """lite-permissions-finder"""

version := "1.0-SNAPSHOT"

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
  cache,
  javaWs,
  filters,
  "redis.clients" % "jedis" % "2.8.1"
)

libraryDependencies += "org.pac4j" % "pac4j" % "1.9.0"
libraryDependencies += "org.pac4j" % "pac4j-saml" % "1.9.0"
libraryDependencies += "org.pac4j" % "play-pac4j" % "2.4.0"

libraryDependencies += "com.typesafe.play.modules" %% "play-modules-redis" % "2.5.0"

libraryDependencies += "uk.gov.bis.lite" % "lite-ogel-service-api" % "1.0"
libraryDependencies += "uk.gov.bis.lite" % "lite-search-management-api" % "1.1"
libraryDependencies += "uk.gov.bis.lite" % "lite-control-code-service-api" % "1.1"

libraryDependencies += "au.com.dius" % "pact-jvm-consumer-junit_2.11" % "3.3.6" % "test"
libraryDependencies += "com.itv" %% "scalapact-scalatest" % "2.1.2" % "test"

resolvers += "Lite Lib Releases " at "http://nexus.mgmt.licensing.service.trade.gov.uk.test/repository/maven-releases/"
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

// Pact
pactBrokerAddress := "http://pact-broker.mgmt.licensing.service.trade.gov.uk.test"
pactContractVersion := "1.0.0"

// Concatenate the /assets/javascript directory into a single lite-permissions-finder.js
Concat.groups := Seq("lite-permissions-finder.js" -> group((sourceDirectory in Assets).value / "javascripts" ** "*.js"))

// Remove assets used in the concat stage. Then applies a manual asset mapping fix to the resulting asset
// lite-permissions-finder.js. This is due to Concat.parentDir changing the new assets location in addition to it's
// asset mapping relative to the web target dir (/target/web/public), causes duplicate assets. Ideally sbt-concat should
// write files out to an intermediate folder (/target/concat) instead.
val filterJs = taskKey[Pipeline.Stage]("filter-web-concat-assets")
filterJs := { (mappings: Seq[PathMapping]) =>
  streams.value.log.info("Filtering javascript assets")
  val liteJs = mappings.find(f => f._2 == "lite-permissions-finder.js").get
  mappings.filter(f => !f._2.startsWith("javascripts")) // Remove assets from the assets/javascript directory
    .toMap
    .updated(liteJs._1, "javascripts/" + liteJs._2) // Replace the asset mapping for lite-permissions-finder.js
    .toSeq
}

// Production and dev pipeline stages
pipelineStages in Assets := Seq(concat, filterJs)

// Production only pipeline stages, (after the above production and dev pipeline stages)
pipelineStages := Seq(digest)