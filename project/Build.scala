import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

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

  val authSubProject = Project("auth", file("auth"))

  val certificates = Project("certificates", file("certificates")).settings(
    libraryDependencies ++= Seq(
      "com.itextpdf" % "itext-xtra" % "5.4.4",
      "org.testng" % "testng" % "6.8" % Test
    )
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    //      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
  ).dependsOn(authSubProject).dependsOn(certificates)

}