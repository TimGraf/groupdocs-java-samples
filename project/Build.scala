import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "groupdocs-java-samples"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "com.groupdocs" % "groupdocs-java-client" % "1.2",
        "com.sun.jersey" % "jersey-core" % "1.13"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
        resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
        templatesImport += "com.groupdocs.sdk.model._",
        templatesImport += "com.groupdocs.sdk.common._"
    )

}
