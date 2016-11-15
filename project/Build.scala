import sbt.Keys._
import sbt._
import scalaprops.ScalapropsPlugin.autoImport._

object Build extends Build {
  val project = "scalacture"

  val basicSettings = Seq(
    version := "0.1",
    scalaVersion := "2.11.8",
    crossScalaVersions := Seq("2.10.5", "2.11.8", "2.12.0")
  )

  lazy val core = Project(
    id = s"$project-core",
    base = file(s"$project-core"),
    settings = basicSettings ++ Seq(
      name := s"$project-core",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "3.0.1" % "test",
        "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
      )
    )
  )

  lazy val scalaz = Project(
    id = s"$project-scalaz",
    base = file(s"$project-scalaz"),
    settings = basicSettings ++ scalapropsWithScalazlaws ++ Seq(
      name := s"$project-scalaz",
      scalapropsVersion := "0.3.4",
      libraryDependencies ++= Seq(
        "org.scalaz" %% "scalaz-core" % "7.2.7"
      )
    )
  ).dependsOn(
    core
  )

  lazy val experimental = Project(
    id = s"$project-experimental",
    base = file(s"$project-experimental"),
    settings = basicSettings ++ Seq(
      name := s"$project-experimental"
    )
  ).dependsOn(
    core,
    core % "test->test"
  )
}
