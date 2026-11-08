// The Play plugin
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.11")

// Defines scaffolding (found under .g8 folder)
// http://www.foundweekends.org/giter8/scaffolding.html
// sbt "g8Scaffold form"
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.18.0")

// Code coverage plugin
addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.7.0")
