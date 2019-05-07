scalaVersion := "2.11.11"

name := "wikipedia-page-processor"
organization := "org.alexeyn"
version := "1.0"

val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion withSources() withJavadoc() exclude("org.spark-project.spark", "unused"),
  "org.apache.spark" %% "spark-sql" % sparkVersion exclude("org.spark-project.spark", "unused"),
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "info.bliki.wiki" % "bliki-core" % "3.1.0",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

enablePlugins(JavaAppPackaging)