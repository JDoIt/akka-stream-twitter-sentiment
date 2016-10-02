Akka streams and Twitter sentiment analysis in Scala
--------------------------


Akka Streams is an implementation of Reactive Streams (http://www.reactive-streams.org/),
which is a standard for asynchronous stream processing with non-blocking backpressure.

Akka Streams provides a way to express and run a chain of asynchronous processing steps acting on a sequence of elements.


To run application you have to generate your twitter key and secret and add an `application.conf` in the `project` folder, with:

appKey = "<your app key>"
appSecret = "<your app secret>"
accessToken = "<your access token>"
accessTokenSecret = "<your access token secret>"


Resources:
  http://doc.akka.io/docs/akka/2.4/scala/stream/stream-cookbook.html