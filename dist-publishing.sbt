import sbt.Keys.publishTo

publish <<= publish dependsOn dist

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
publishTo := Some("Sonatype Nexus" at "http://nexus.mgmt.licensing.service.trade.gov.uk.test/repository/raw-test")

publishMavenStyle := true

// Disable unnecessary artifacts
publishArtifact in(Compile, packageBin) := false
publishArtifact in(Compile, packageDoc) := false
publishArtifact in(Compile, packageSrc) := false
