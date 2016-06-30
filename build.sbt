name := "chat-exercise"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in (Compile, run) := Some("Hi")

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.1"
)
