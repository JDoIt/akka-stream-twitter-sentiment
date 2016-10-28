package ru.zrg.akka.stream.twitter.sentiment

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import scala.collection.JavaConversions._

trait SentimentType {
  def name: String
  override def toString = name
}

object SentimentType {

  def apply(sentimentId: Int) = sentimentId match {
    case 0 => VERY_NEGATIVE
    case 1 => NEGATIVE
    case 2 => NEUTRAL
    case 3 => POSITIVE
    case 4 => VERY_POSITIVE
    case _ => NEUTRAL
  }

}

case object VERY_NEGATIVE extends SentimentType { val name = "very negative"}
case object NEGATIVE extends SentimentType { val name = "negative"}
case object NEUTRAL extends SentimentType { val name = "neutral"}
case object POSITIVE extends SentimentType { val name = "positive"}
case object VERY_POSITIVE extends SentimentType { val name = "very positive"}

object SentimentAnalysisAPI {

  val props = new Properties();
  props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
  val pipeline = new StanfordCoreNLP(props);


  def findSentiment(message: String): SentimentType = Option(message) match {
    case Some(text) if !text.isEmpty => extractSentiment(text)
    case _ => throw new IllegalArgumentException("input can't be null or empty")
  }

  private def extractSentiment(message: String): SentimentType = {

    val annotation = pipeline.process(message);
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation]);

    val sentiments = sentences.map(sentence => sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree]))
      .map(tree => RNNCoreAnnotations.getPredictedClass(tree))

    val resultSentiment = math.ceil(sentiments.foldLeft(0.0) {_ + _} / sentiments.length).toInt

    SentimentType(resultSentiment)
  }
}
