import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "bbts.contest"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "org.mongodb" % "mongo-java-driver" % "2.9.2", //mongo db driver
      "org.apache.commons" % "commons-email" % "1.2", //simple email wrapper
      "javax.mail" % "mail" % "1.4.5",  //not sure this needed, it may already be in dependencies
      "net.sf.opencsv" % "opencsv" % "2.3" // CSV reader and writer http://opencsv.sourceforge.net
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
    )

}
