name := "akka-stream-twitter-sentiment"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in (Compile, run) := Some("ru.zrg.akka.stream.twitter.sentiment.TwitterSentimentAnalyzer")

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-stream_2.11" % "2.4.10",
  "org.twitter4j" % "twitter4j-core" % "4.0.5",
  "org.twitter4j" % "twitter4j-stream" % "4.0.5",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0"  classifier "models"
)