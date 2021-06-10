name := "capstone"

version := "0.1"

scalaVersion := "2.13.6"


libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-actor" % "2.6.14",
  "com.typesafe.akka" %% "akka-http" % "10.2.4",
  "com.typesafe.akka" %% "akka-stream" % "2.6.14",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.4",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "mysql" % "mysql-connector-java" % "8.0.25",
  "com.pauldijou" %% "jwt-spray-json" % "5.0.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.14" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.4" % Test,
  "org.scalatest" %% "scalatest" % "3.2.3" % Test,
  "org.mockito" % "mockito-core" % "3.6.28" % Test
)