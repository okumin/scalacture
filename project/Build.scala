import sbt._
import sbt.Keys._

object Build extends Build {
  val project = "scalacture"

  lazy val core = Project(
    id = s"$project-core",
    base = file(s"$project-core"),
    settings = Seq(
      name := s"$project-core",
      version := "1.0",
      scalaVersion := "2.11.6",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.4" % "test",
        "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"
      )
    )
  )
}
