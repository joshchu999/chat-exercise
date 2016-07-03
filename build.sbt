name := "chat-exercise"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in (Compile, run) := Some("idv.trashchu.exercise.chat.ChatApplication")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.7",
  "com.typesafe.akka" %% "akka-remote" % "2.4.7"
)
