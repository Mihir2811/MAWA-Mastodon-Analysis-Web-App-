import scala.sys.process.ProcessLogger

name := """soen6441groupproject"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  guice,
  "org.mockito" % "mockito-core" % "5.14.2" % Test
)

jacocoExcludes := Seq(
  "views.html.*",
  "router.*",
  "controllers.javascript.*",
  "controllers.Reverse*",
  "controllers.routes*",
  "Module*"
)

// ---------------------------------------------------------------------------
// `sbt javadoc`: HTML Javadoc for the application *and* test sources, including
// private members (the project submission requires the private members too).
// Output: target/javadoc/index.html
// ---------------------------------------------------------------------------
lazy val javadoc = taskKey[File]("Generates HTML Javadoc for app and test sources, including private members")

javadoc := {
  val log = streams.value.log
  val outputDir = target.value / "javadoc"
  val appSources = (Compile / javaSource).value
  val testSources = (Test / javaSource).value
  val sourceFiles = (appSources ** "*.java").get ++ (testSources ** "*.java").get
  val classpath = ((Compile / dependencyClasspath).value ++ (Test / dependencyClasspath).value)
    .files.map(_.getAbsolutePath).distinct
  val javadocExecutable = file(System.getProperty("java.home")) / "bin" / "javadoc"

  IO.delete(outputDir)
  IO.createDirectory(outputDir)

  val arguments = Seq(
    javadocExecutable.getAbsolutePath,
    "-d", outputDir.getAbsolutePath,
    "-private",                        // include private fields and methods
    "-Xdoclint:all,-missing",          // keep real problems, ignore "missing comment" style notes
    "-quiet",
    "-encoding", "UTF-8",
    "-sourcepath", Seq(appSources.getAbsolutePath, testSources.getAbsolutePath).mkString(java.io.File.pathSeparator),
    "-classpath", classpath.mkString(java.io.File.pathSeparator)
  ) ++ sourceFiles.map(_.getAbsolutePath)

  val exitCode = scala.sys.process.Process(arguments).!(
    ProcessLogger(message => log.info(message), message => log.error(message))
  )
  if (exitCode != 0) {
    sys.error("javadoc failed with exit code " + exitCode)
  }
  log.info("Javadoc generated at " + outputDir)
  outputDir
}
