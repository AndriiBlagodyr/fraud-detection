package ua.ucu.edu

import java.util.Properties
import java.util.concurrent.TimeUnit

import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.slf4j.LoggerFactory

// dummy app for testing purposes
object DummyStreamingApp extends App {

  val logger = LoggerFactory.getLogger(getClass)

  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streaming_app")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv(Config.KafkaBrokers))
  props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, Long.box(5 * 1000))
  props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, Long.box(0))

  import Serdes._

  val builder = new StreamsBuilder

  //TODO - two KTables black ip and email patterns
  //val blackDataStream = builder.stream[String, String](System.getenv(Config.EnrichmentTopic))
  val userActivityStream = builder.stream[String, String](System.getenv(Config.MainTopic))

  userActivityStream.peek((k, v) => {
    logger.info(s"record processed $k->$v")
  }).to(System.getenv(Config.EnrichedTopic))

  val streams = new KafkaStreams (builder.build (), props)
  streams.cleanUp ()
  streams.start ()

  sys.addShutdownHook {
  streams.close (10, TimeUnit.SECONDS)
}

  object Config {
    val KafkaBrokers = "KAFKA_BROKERS"
    val MainTopic = "MAIN_TOPIC"
    val EnrichmentTopic = "ENRICHMENT_TOPIC"
    val EnrichedTopic = "ENRICHED_TOPIC"
  }

}
