import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play-redis"
  val appVersion      = "0.1-SNAPSHOT"

  val appDependencies = Seq(
    "net.debasishg" % "redisclient_2.10" % "2.10",
    "com.twitter" % "chill_2.10" % "0.2.3"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
  )

}
