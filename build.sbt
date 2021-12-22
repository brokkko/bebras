import sbt._
import Keys._
import com.typesafe.sbt.packager.linux.LinuxSymlink
import com.typesafe.sbt.packager.MappingsHelper._

logLevel := Level.Debug
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
    "org.testng" % "testng" % "6.8" % Test,
    "net.sf.opencsv" % "opencsv" % "2.3"
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
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "org.mozilla" % "rhino" % "1.7.13"
  )
)

lazy val packagingSettings = Seq(
  packageName in Linux := sys.props.getOrElse("packageName", "dces2"),

  mappings in Universal :=
    (mappings in Universal).value filter {
      //remove everything from /conf folder except specific files
      case (mappedFile, _) =>
        def fileHasExample(f: File): Boolean = {
          val exampleFile = new File(f.getParentFile, f.getName + ".example")
          exampleFile.exists()
        }

        mappedFile.getParentFile.getName != "conf" || !fileHasExample(mappedFile)
    },
  mappings in Universal ++= directory("scripts"),
  mappings in Universal ++= directory("public"),

  linuxPackageMappings += packageTemplateMapping(s"/var/lib/${(packageName in Linux).value}")(),
//  daemonUser := "", //default Daemon user = package name
  linuxPackageSymlinks += LinuxSymlink(s"/usr/share/${(packageName in Linux).value}/data", s"/var/lib/${(packageName in Linux).value}")
)

lazy val debianPackaging = Seq(
  maintainer := "Ilya Posov <iposov@gmail.com>",
  packageSummary := "Dces2 Debian Package",
  packageDescription := """Dces2 system""",
  debianPackageDependencies ++= Seq("wkhtmltopdf", "xvfb")
)

lazy val fedoraPackaging = Seq(
  rpmVendor := "kio",
  rpmUrl := Some("http://ipo.spb.ru"),
  rpmLicense := Some("MIT"),
  rpmRelease := sys.props.getOrElse("rpmver", "1"),
  rpmRequirements ++= Seq("wkhtmltopdf", "xorg-x11-server-Xvfb")
)

lazy val dces2 = project.in(file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := "dces2",
    version := "0.4.112", // [VERSION] do not remove this comment, it is used by ansible to retrieve program version
    scalaVersion := "2.11.8",
    // Add your own project settings here
    //      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

    sources in doc in Compile := Seq(), //do not compile documentation

    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),

    libraryDependencies ++= Seq(
      cache,
      javaWs,
      // Add your project dependencies here,
      "org.mongodb" % "mongo-java-driver" % "3.4.2",
      "org.apache.commons" % "commons-email" % "1.2", //simple email wrapper
      "javax.mail" % "mail" % "1.4.5", //not sure this needed, it may already be in dependencies
      "net.sf.opencsv" % "opencsv" % "2.3", // CSV reader and writer http://opencsv.sourceforge.net
      "com.itextpdf" % "itext-xtra" % "5.4.4",
      "com.google.zxing" % "core" % "3.4.1",
      "com.google.zxing" % "javase" % "3.4.1"
    ),

    //packaging
    packagingSettings,

    sys.props.get("packageType") match {
      case Some("ubuntu14") =>
        println("generating ubuntu 14 package")
          SystemloaderPlugin.projectSettings ++ UpstartPlugin.projectSettings ++ debianPackaging
      case Some("fedora") =>
        println("generating fedora package")
        SystemloaderPlugin.projectSettings ++ SystemdPlugin.projectSettings ++ fedoraPackaging
      case _ =>
        Seq()
    }

  ).aggregate(authSubProject).aggregate(certificates).aggregate(kioJsProblems)
  .dependsOn(authSubProject).dependsOn(certificates).dependsOn(kioJsProblems)

//Keys.fork := true

//TODO aggreate and dependsOn together?
//TODO auth sub project not visible
//TODO where to define scala version?

//change inside /usr/lib/rpm/redhat/macros: w19.zstdio to w9.gzdio
//the same inside /usr/lib/rpm/macros. %_source_payload and %_binary_payload
//to test: rpm --eval %{_binary_payload}
