import sbt._
import Keys._
import com.typesafe.sbt.packager.linux.LinuxSymlink
import com.typesafe.sbt.packager.MappingsHelper._

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
  ),

  //From SO. Disable JavaDoc (non utf8 symbols in path)
  publishArtifact in(Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
  sources in(Compile, doc) := Seq.empty
)

lazy val kioJsProblems = Project("kio-js-problems", file("kio-js-problems")).settings(
  name := "kio-js-problems",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test
  )
)

lazy val packagingSettings = Seq(
  packageName in Linux := "dces2",

  mappings in Universal :=
    (mappings in Universal).value filter {
      //remove everything from /conf folder except specific files
      case (file, _) =>
        file.getParentFile.getName != "conf" || file.getName.endsWith("example")
    },
  mappings in Universal ++= directory("scripts"),
  mappings in Universal ++= directory("public"),

  linuxPackageMappings += packageTemplateMapping(s"/var/lib/${packageName.value}")(),
  linuxPackageSymlinks += LinuxSymlink(s"/usr/share/${packageName.value}/data", s"/var/lib/${packageName.value}")
)

lazy val dces2 = project.in(file("."))
  .enablePlugins(JavaServerAppPackaging, PlayJava)
  .settings(
    name := "dces2",
    version := "0.4.0",
    scalaVersion := "2.11.8",
    // Add your own project settings here
    //      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

    sources in doc in Compile := Seq(), //do not compile documentation

    packagingSettings,

    libraryDependencies ++= Seq(
      cache,
      javaWs,
      // Add your project dependencies here,
      "org.mongodb" % "mongo-java-driver" % "3.4.2",
      "org.apache.commons" % "commons-email" % "1.2", //simple email wrapper
      "javax.mail" % "mail" % "1.4.5", //not sure this needed, it may already be in dependencies
      "net.sf.opencsv" % "opencsv" % "2.3", // CSV reader and writer http://opencsv.sourceforge.net
      "com.itextpdf" % "itext-xtra" % "5.4.4"
    )
  ).aggregate(authSubProject).aggregate(certificates).aggregate(kioJsProblems)
  .dependsOn(authSubProject).dependsOn(certificates).dependsOn(kioJsProblems)

lazy val ubuntu14_package = dces2.copy(id="ubuntu14_package").in(file("build/ubuntu14"))
  .enablePlugins(JavaServerAppPackaging, UpstartPlugin)
  .settings(
    serverLoading := Some(ServerLoader.Upstart),

    maintainer := "Ilya Posov <iposov@gmail.com>",
    packageSummary := "Dces2 Debian Package",
    packageDescription := """Dces2 system""",

    debianPackageDependencies += "wkhtmltopdf"
  )

lazy val fedora_package = dces2.copy(id="fedora_package").in(file("build/fedora"))
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin)
  .settings(
    serverLoading := Some(ServerLoader.Systemd),

    rpmVendor := "kio",
    rpmUrl := Some("http://ipo.spb.ru"),
    rpmLicense := Some("MIT"),
    rpmRelease := "13",
    rpmRequirements += "wkhtmltopdf"
  )

//Keys.fork := true

//TODO aggreate and dependsOn together?
//TODO auth sub project not visible
//TODO where to define scala version?
