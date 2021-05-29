package com.github.streams.datatype

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonSerializer, ObjectMapper, SerializerProvider}
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class TeamStat(season: Int, name: String, played:Int, goalsFor: Int, goalsAgainst: Int, goalsDiff: Int, points: Int) extends Serializable


class TeamStatDeserializer extends Deserializer[TeamStat] {
  override def deserialize(topic: String, data: Array[Byte]): TeamStat = {
    JsonSupport.toInstance(data, classOf[TeamStat])
  }
}

class TeamStatSerializer extends Serializer[TeamStat] {
  override def serialize(topic: String, data: TeamStat): Array[Byte] = {
    JsonSupport.toBytes(data)
  }
}

class TeamStatSerde extends Serde[TeamStat] {
  override def serializer(): Serializer[TeamStat] = new TeamStatSerializer()

  override def deserializer(): Deserializer[TeamStat] = new TeamStatDeserializer()
}

case class MatchResult(round: Int, season: Int, name: String, opponent: String, goalsFor: Int, goalsAgainst: Int, points: Int)
  extends Serializable

class MatchResultDeserializer extends Deserializer[MatchResult] {
  override def deserialize(topic: String, data: Array[Byte]): MatchResult = {
    JsonSupport.toInstance(data, classOf[MatchResult])
  }
}

class MatchResultSerializer extends Serializer[MatchResult] {
  override def serialize(topic: String, data: MatchResult): Array[Byte] = {
    JsonSupport.toBytes(data)
  }
}

class MatchResultSerde extends Serde[MatchResult] {
  override def serializer(): Serializer[MatchResult] = new MatchResultSerializer()

  override def deserializer(): Deserializer[MatchResult] = new MatchResultDeserializer()
}

case class FootballMatchEvent(
                               round: Int,
                               matchDate: LocalDate,
                               home: String,
                               away: String,
                               score: String,
                               season: Int,
                               homeScore: Int,
                               awayScore: Int,
                               homePoints: Int,
                               awayPoints: Int
                             ) extends Serializable

class FootballMatchEventDeserializer extends Deserializer[FootballMatchEvent] {
  override def deserialize(topic: String, data: Array[Byte]): FootballMatchEvent = {
    JsonSupport.toInstance(data, classOf[FootballMatchEvent])
  }
}

class FootballMatchEventSerializer extends Serializer[FootballMatchEvent] {
  override def serialize(topic: String, data: FootballMatchEvent): Array[Byte] = {
    JsonSupport.toBytes(data)
  }
}

class FootballMatchSerde extends Serde[FootballMatchEvent] {
  override def serializer(): Serializer[FootballMatchEvent] = new FootballMatchEventSerializer()

  override def deserializer(): Deserializer[FootballMatchEvent] = new FootballMatchEventDeserializer()
}

class LocalDateSerialiser extends JsonSerializer[LocalDate] {
  override def serialize(value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    val data: String = Option(value).map(_.format(DateTimeFormatter.ISO_LOCAL_DATE)).getOrElse(null)
    gen.writeRawValue(s""""$data"""")
  }

  override def handledType(): Class[LocalDate] = classOf[LocalDate]
}

class LocalDateDeserialiser extends JsonDeserializer[LocalDate] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate = {
    LocalDate.parse(p.getText, DateTimeFormatter.ISO_LOCAL_DATE)
  }

  override def handledType(): Class[LocalDate] = classOf[LocalDate]
}

class JavaDateTimeModule extends SimpleModule {
  addSerializer(new LocalDateSerialiser())
  addDeserializer(classOf[LocalDate], new LocalDateDeserialiser())
}

object JsonSupport {
  val om = new ObjectMapper()
  om.registerModule(DefaultScalaModule)
  om.registerModule(new JavaDateTimeModule())

  def toJson(m: AnyRef): String = om.writeValueAsString(m)

  def toBytes(m: AnyRef): Array[Byte] = om.writeValueAsBytes(m)

  def toInstance[T](m: Array[Byte], clz: Class[T]): T = om.readValue[T](m, clz)

  def toInstance[T](m: String, clz: Class[T]): T = toInstance(m.getBytes, clz)
}
