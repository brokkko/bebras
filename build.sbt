import sbt._
import Keys._
import play.Project._

lazy val authSubProject = project.in(file("auth"))

lazy val certificates = project.in(file("certificates")).settings(
  libraryDependencies ++= Seq(
    "com.itextpdf" % "itext-xtra" % "5.4.4",
    "org.testng" % "testng" % "6.8" % Test
  )
)

lazy val kioJsProblems = Project("kio-js-problems", file("kio-js-problems")).settings(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test
  )
)

val appName = "dces2"
val appVersion = "0.3.0"

val appDependencies = Seq(
  javaCore,
  cache,
  // Add your project dependencies here,
  "org.mongodb" % "mongo-java-driver" % "3.1.0", //mongo db driver, was 2.9.2
  "org.apache.commons" % "commons-email" % "1.2", //simple email wrapper
  "javax.mail" % "mail" % "1.4.5", //not sure this needed, it may already be in dependencies
  "net.sf.opencsv" % "opencsv" % "2.3", // CSV reader and writer http://opencsv.sourceforge.net
  "com.itextpdf" % "itext-xtra" % "5.4.4"
  //      "com.github.mumoshu" %% "play2-memcached" % "0.3.0.2"
)

lazy val root = play.Project(appName, appVersion, appDependencies).settings(
//  unmanagedJars in compile += file("/usr/lib/jvm/java-1.8.0-openjdk/nashorn.jar"),
  Keys.fork := true
  // Add your own project settings here
  //      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
).dependsOn(authSubProject).dependsOn(certificates).dependsOn(kioJsProblems)

Keys.fork := true