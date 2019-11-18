import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.OAuthAuthorization
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import Sentiment._
import scala.collection.convert.wrapAll._

object SentimentAnalysis {
  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  def main(args: Array[String]): Unit = {
    if(args.length < 1){
      System.err.println("Usage: SentimentAnalysis <outputTopic>")
    }
    System.setProperty("hadoop.home.dir", if (args.length > 1) args(1) else "C:\\Users\\kavya\\Downloads\\kafka\\a")
    val outTopic = if (args.length > 0) args(0) else "topicB"
    val filter = "movie"

    val sparkConf = new SparkConf().setAppName("TwitterSentimentAnalysis")
    if (!sparkConf.contains("spark.master")) {
      sparkConf.setMaster("local[2]")
    }

    val apiKey = "lsGTkpo0BWIHLNrkX0bEogYDW"
    val apiSecret = "TwcmTwi1AxFqsfaqD9sT4qGA75JZWxWcP0K2JhET5GWXbJbhXu"
    val accessKey = "970629511-HN7D61IIlfI34eKgqt6LuiuAIW6lN2knK3hL4VeA"
    val accessSecret = "FhNBaoLxo5CaEvEY4WvoECmD47Vhj0jnQnXsdaR9RKUYP"

    val ssc = new StreamingContext(sparkConf, Seconds(1))
    val cb = new ConfigurationBuilder
    cb.setDebugEnabled(true).setOAuthConsumerKey(apiKey)
      .setOAuthConsumerSecret(apiSecret)
      .setOAuthAccessToken(accessKey)
      .setOAuthAccessTokenSecret(accessSecret)
    val auth = new OAuthAuthorization(cb.build)
    val stream = TwitterUtils.createStream(ssc, Some(auth),Array(filter))



    def kafkaConfig(topic: String) = {
      val serializer = "org.apache.kafka.common.serialization.StringSerializer"
      val props = new Properties()
      props.put("bootstrap.servers", "localhost:9092")
      props.put("key.serializer", serializer)
      props.put("value.serializer", serializer)
      props
    }

    stream.filter(tweet => tweet.getLang.equals("en")).foreachRDD(rdd => {
      val producer = new KafkaProducer[String, String](kafkaConfig(outTopic))
      rdd.collect().toList.foreach(a=> {
        val (sentence, sentiment) = mainSentiment(a.getText)
        producer.send(new ProducerRecord[String, String](outTopic, sentence, sentiment.toString()))
      })
    })

    ssc.checkpoint("/checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }

  def mainSentiment(input: String): (String, Sentiment) = Option(input) match {
    case Some(text) if !text.isEmpty => extractSentiment(text)
    case _ => throw new IllegalArgumentException("input can't be null or empty")
  }

  private def extractSentiment(text: String): (String, Sentiment) = {
    val (sentence, sentiment) = extractSentiments(text)
      .maxBy { case (sentence, _) => sentence.length }
    (sentence, sentiment)
  }

  def extractSentiments(text: String): List[(String, Sentiment)] = {
    val annotation: Annotation = pipeline.process(text)
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
    sentences
      .map(sentence => (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])))
      .map { case (sentence, tree) => (sentence.toString,Sentiment.toSentiment(RNNCoreAnnotations.getPredictedClass(tree))) }
      .toList
  }

}
