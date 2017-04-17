import sbt._
import Keys._

scalaVersion := "2.11.8"

lazy val authSubProject = project.in(file("auth")).settings(
  name := "authSubProject",
  version := "0.1.0",
  scalaVersion := "2.11.8"
)

lazy val certificates = project.in(file("certificates")).settings(
  name := "certificates",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "com.itextpdf" % "itext-xtra" % "5.4.4",
    "org.testng" % "testng" % "6.8" % Test
  )
)

lazy val kioJsProblems = Project("kio-js-problems", file("kio-js-problems")).settings(
  name := "kio-js-problems",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test
  )
)

lazy val dces2 = project.in(file(".")).enablePlugins(PlayJava).settings(
  name := "dces2",
  version := "0.4.0",
  scalaVersion := "2.11.8",
  // Add your own project settings here
  //      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

  libraryDependencies ++= Seq(
    cache,
    javaWs,
    // Add your project dependencies here,
    "org.mongodb" % "mongo-java-driver" % "3.1.0", //mongo db driver, was 2.9.2
    "org.apache.commons" % "commons-email" % "1.2", //simple email wrapper
    "javax.mail" % "mail" % "1.4.5", //not sure this needed, it may already be in dependencies
    "net.sf.opencsv" % "opencsv" % "2.3", // CSV reader and writer http://opencsv.sourceforge.net
    "com.itextpdf" % "itext-xtra" % "5.4.4"
  )
).aggregate(authSubProject).aggregate(certificates).aggregate(kioJsProblems)
 .dependsOn(authSubProject).dependsOn(certificates).dependsOn(kioJsProblems)
//Keys.fork := true
