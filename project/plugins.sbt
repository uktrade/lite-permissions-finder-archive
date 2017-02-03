// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.9")

// Web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.2")

// Play enhancer - this automatically generates getters/setters for public fields
// and rewrites accessors of these fields to use the getters/setters. Remove this
// plugin if you prefer not to have this feature, or disable on a per project
// basis using disablePlugins(PlayEnhancer) in your build.sbt
addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.1.0")

// Override sbt-play-enhancer use of javassist 3.18.2-GA, which fixes JASSIST-220
// https://github.com/jboss-javassist/javassist/pull/10
libraryDependencies += "org.javassist" % "javassist" % "3.20.0-GA"

// Play Ebean support, to enable, uncomment this line, and enable in your build.sbt using
// enablePlugins(PlayEbean).
// addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "1.0.0")

//For version info in the build
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")