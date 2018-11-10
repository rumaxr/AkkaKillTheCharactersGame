name := "AkkaKillTheCharactersGame"

version := "1.0"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.18",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.18" % Test,
  "commons-cli" % "commons-cli" % "1.4"
)
