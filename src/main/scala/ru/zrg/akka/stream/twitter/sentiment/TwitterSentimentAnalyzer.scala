package ru.zrg.akka.stream.twitter.sentiment

import java.util.concurrent.{Executors, ExecutorService}


import akka.NotUsed
import akka.actor.{ActorRef, Props, ActorSystem}
import akka.stream.{ClosedShape, ActorMaterializer}

import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext

object TwitterSentimentAnalyzer extends App{


  val execService: ExecutorService = Executors.newCachedThreadPool()
  implicit val system: ActorSystem = ActorSystem("sentiment")
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(execService)
  implicit val materializer = ActorMaterializer()(system)


  val twitterStream = new TwitterStreamClient(system)
  twitterStream.init

  val tweets: Source[Tweet, ActorRef] = Source.actorPublisher(Props[StatusPublisherActor])

  val out = Sink.foreach[( SentimentType, Tweet, Long)]({case (sentiment, tweet, count) =>
    println(s"$count - $sentiment. Tweet: ${tweet.body} - ${tweet.author.handle}")
  })

  val filterLang = Flow[Tweet].filter(t => Option(t.lang) match {
    case Some(lang) if "en" equalsIgnoreCase lang => true
    case _ => false
  })

  val negativeSentiment = Flow[( SentimentType, Tweet, Long)].filter(tuple =>
    tuple._1 == VERY_NEGATIVE || tuple._1 == NEGATIVE)

  val positiveSentiment = Flow[(SentimentType, Tweet, Long)].filter(tuple =>
    tuple._1 == VERY_POSITIVE || tuple._1 == POSITIVE)

  val mapToSentiment = Flow[Tweet].map(t => ( SentimentAnalysisAPI.findSentiment(t.body), t, 0L))

  val sum = Flow[(SentimentType, Tweet, Long)].scan[(SentimentType, Tweet, Long)] (NEUTRAL, EmptyTweet, 0L)(
    (state, newValue) => ( newValue._1,  newValue._2, state._3 + 1L))

  val graphSentiment = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val bcast = builder.add(Broadcast[( SentimentType, Tweet, Long)](2))

    tweets ~> filterLang ~> mapToSentiment ~> bcast ~> negativeSentiment ~> sum ~> out
                                              bcast ~> positiveSentiment ~> sum ~> out

    ClosedShape
  })

  graphSentiment.run

}
