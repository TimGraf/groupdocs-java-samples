import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "groupdocs-java-samples"
    val appVersion      = "1.8.1"

    val appDependencies = Seq(
        "com.sun.jersey" % "jersey-core" % "1.13",
        "javax.mail" % "mail" % "1.4.5",
        "log4j" % "log4j" % "1.2.11",
        "org.apache.commons" % "commons-io" % "1.3.2",
        "com.sun.jersey" % "jersey-client" % "1.9.1",
        "com.groupdocs" % "groupdocs-java-client" % "1.8.1",
        "com.typesafe" %% "play-plugins-mailer" % "2.1-RC2"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
        resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
        templatesImport += "com.groupdocs.sdk.model._",
        templatesImport += "com.groupdocs.sdk.common._"
    )
}
