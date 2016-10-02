package ru.zrg.akka.stream.twitter.sentiment

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import twitter4j._
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder

import scala.collection.JavaConverters._
import scala.collection._
import scala.util.Try

final case class Author(handle: String)

final case class Hashtag(name: String)

case class Tweet(author: Author, timestamp: Long, body: String, place: Place, lang: String) {
  def hashtags: Set[Hashtag] =
    body.split(" ").collect { case t if t.startsWith("#") => Hashtag(t)}.toSet
}

final object EmptyTweet extends Tweet(Author(""), 0L, "", null, "")

object CretentialsUtils {
  val config = ConfigFactory.load()
  val appKey: String = config.getString("appKey")
  val appSecret: String = config.getString("appSecret")
  val accessToken: String = config.getString("accessToken")
  val accessTokenSecret: String = config.getString("accessTokenSecret")
}

object TwitterClient {

  def apply(): Twitter = {
    val factory = new TwitterFactory(new ConfigurationBuilder().build())
    val t = factory.getInstance()
    t.setOAuthConsumer(CretentialsUtils.appKey, CretentialsUtils.appSecret)
    t.setOAuthAccessToken(new AccessToken(CretentialsUtils.accessToken, CretentialsUtils.accessTokenSecret))
    t
  }
}

class TwitterStreamClient(val actorSystem: ActorSystem) {
  val factory = new TwitterStreamFactory(new ConfigurationBuilder().build())
  val twitterStream = factory.getInstance()

  def init = {
    twitterStream.setOAuthConsumer(CretentialsUtils.appKey, CretentialsUtils.appSecret)
    twitterStream.setOAuthAccessToken(new AccessToken(CretentialsUtils.accessToken, CretentialsUtils.accessTokenSecret))
    twitterStream.addListener(simpleStatusListener)
    twitterStream.sample
  }

  def simpleStatusListener = new StatusListener() {
    def onStatus(s: Status) {
      actorSystem.eventStream.publish(Tweet(Author(s.getUser.getScreenName), s.getCreatedAt.getTime, s.getText, s.getPlace, s.getLang))
    }

    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}

    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}

    def onException(ex: Exception) {
      ex.printStackTrace
    }

    def onScrubGeo(arg0: Long, arg1: Long) {}

    def onStallWarning(warning: StallWarning) {}
  }

  def stop = {
    twitterStream.cleanUp
    twitterStream.shutdown
  }

}

