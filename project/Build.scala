import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "bbts.contest"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "org.mongodb" % "mongo-java-driver" % "2.9.2"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
    )

}
