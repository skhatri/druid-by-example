package com.github.streams.configuration

import com.github.streams.datatype.FootballMatchSerde
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.apache.kafka.common.serialization.{ByteArraySerializer, Serdes, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.StreamsConfig

import java.util.{Properties, UUID}
import scala.jdk.CollectionConverters._

object Settings {

  private val producerMap: Map[String, AnyRef] = kafkaBrokerProps ++ Map[String, AnyRef](
    ProducerConfig.CLIENT_ID_CONFIG -> "team-producer",
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer].getName,
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer].getName,
  )

  lazy val producer = new KafkaProducer[String, String](producerMap.asJava)

  val streamMap = (kafkaBrokerProps ++ Map[String, AnyRef](
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
    StreamsConfig.APPLICATION_ID_CONFIG -> ("stream-consumer-id" + UUID.randomUUID().toString),
    StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG -> Serdes.String().getClass,
    StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG -> classOf[FootballMatchSerde],
    StreamsConfig.STATE_DIR_CONFIG -> "kafka-streams-state",
    StreamsConfig.REPLICATION_FACTOR_CONFIG -> "3"
  )).asJava


  lazy val streamProps: Properties = {
    val props = new Properties()
    Settings.streamMap.asScala.map(kv => props.put(kv._1, kv._2))
    props
  }

  lazy val kafkaBrokerProps: Map[String, AnyRef] = {
    val kafkaConfig = ConfigFactory.load("secrets").getObject("kafka-local").unwrapped()
    kafkaConfig.asScala.toMap
  }

}
