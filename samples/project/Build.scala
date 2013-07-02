import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "samples"
  val appVersion      = "0.1-SNAPSHOT"

  val appDependencies = Seq(
    "securesocial" %% "securesocial" % "master-SNAPSHOT",
    "play-redis" %% "play-redis" % "0.1-SNAPSHOT"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq(
      Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
    )
  )

}
