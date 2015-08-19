import sbt._
import sbt.Keys._

object Build extends Build {
  val project = "scalacture"

  val basicSettings = Seq(
    version := "0.1",
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.10.5", "2.11.7")
  )

  lazy val core = Project(
    id = s"$project-core",
    base = file(s"$project-core"),
    settings = basicSettings ++ Seq(
      name := s"$project-core",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.4" % "test",
        "org.scalacheck" %% "scalacheck" % "1.12.4" % "test"
      )
    )
  )
}
