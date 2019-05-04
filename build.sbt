val projectName = "scalacture"

val basicSettings = Seq(
  version := "0.1",
  scalaVersion := "2.11.12",
  crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8")
)

lazy val core = Project(
  id = s"$projectName-core",
  base = file(s"$projectName-core")
).settings(
  basicSettings,
  name := s"$projectName-core",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.8-RC2" % "test",
    "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
  )
)

lazy val scalaz = Project(
  id = s"$projectName-scalaz",
  base = file(s"$projectName-scalaz")
).settings(
  basicSettings,
  scalapropsWithScalaz,
  name := s"$projectName-scalaz",
  scalapropsVersion := "0.6.0",
  libraryDependencies ++= Seq(
    "org.scalaz" %% "scalaz-core" % "7.2.27"
  )
).dependsOn(
  core
)

lazy val experimental = Project(
  id = s"$projectName-experimental",
  base = file(s"$projectName-experimental")
).settings(
  basicSettings,
  name := s"$projectName-experimental"
).dependsOn(
  core,
  core % "test->test"
)
