import sbt.Keys.publishTo

publish <<= publish dependsOn dist

val nexusUsername = Option(System.getProperty("nexusUsername")).getOrElse("")

val nexusPassword = Option(System.getProperty("nexusPassword")).getOrElse("")

val publishDist = TaskKey[File]("publish-dist", "Publish the dist zip rather than just the jar")
artifact in publishDist ~= { (art: Artifact) => art.copy(`type` = "zip", extension = "zip") }

val publishDistSettings = Seq[Setting[_]](
  publishDist <<= (target in Universal, normalizedName, version) map { (targetDir, id, version) =>
    val packageName = "%s-%s" format(id, version)
    targetDir / (packageName + ".zip")
  }) ++ Seq(addArtifact(artifact in publishDist, publishDist).settings: _*)

Seq(publishDistSettings: _*)

// disable using the Scala version in output paths and artifacts
crossPaths := false

// Publish to the LITE Nexus sbt-dist repository
// Note: repo name must be exactly correct? https://stackoverflow.com/questions/44466800/how-to-configure-sbt-to-publish-to-private-nexus-maven-repo
publishTo := Some("Sonatype Nexus Repository Manager" at "https://nexus.ci.uktrade.io/repository/lite-builds-raw")
credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.ci.uktrade.io", nexusUsername, nexusPassword)

publishMavenStyle := true

// Disable unnecessary artifacts
publishArtifact in(Compile, packageBin) := false
publishArtifact in(Compile, packageDoc) := false
publishArtifact in(Compile, packageSrc) := false
