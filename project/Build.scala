import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "dces2"
    val appVersion      = "0.2.5"

    val appDependencies = Seq(
      javaCore,
      // Add your project dependencies here,
      "org.mongodb" % "mongo-java-driver" % "2.9.2", //mongo db driver
      "org.apache.commons" % "commons-email" % "1.2", //simple email wrapper
      "javax.mail" % "mail" % "1.4.5",  //not sure this needed, it may already be in dependencies
      "net.sf.opencsv" % "opencsv" % "2.3" // CSV reader and writer http://opencsv.sourceforge.net
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}