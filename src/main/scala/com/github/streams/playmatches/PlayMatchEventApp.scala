package com.github.streams.playmatches

import com.github.streams.configuration.Settings
import com.github.streams.datatype.JsonSupport
import com.opencsv.CSVParser
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.nio.file.{Files, Path}
import java.time.{LocalDate, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.Locale
import scala.jdk.CollectionConverters.IteratorHasAsScala

case class FootballMatch(round: Int, matchDate: LocalDate, home: String, away: String, score: String, season: Int) {
  val timestamp = matchDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli
  val (homeScore, awayScore, homePoints, awayPoints) = {
    val parts = score.split("-")
    val homeScore = parts(0).toInt
    val awayScore = parts(1).toInt
    val (homePoints, awayPoints) = if (homeScore > awayScore) (3, 0) else if (homeScore == awayScore) (1, 1) else (0, 3)
    (homeScore, awayScore, homePoints, awayPoints)
  }

  def toTeamData():Seq[TeamData] = {
    val homeTeam = TeamData(matchDate, timestamp, season, round, home, homeScore, away, awayScore, homePoints)
    val awayTeam = TeamData(matchDate, timestamp, season, round, away, awayScore, home, homeScore, awayPoints)
    Seq(
      homeTeam,
      awayTeam
    )
  }

  override def toString() = s"""$round,$home,$away,$score,$homePoints,$awayPoints,$homeScore,$awayScore"""
}

case class TeamData(matchDate:LocalDate, timestamp:Long, season:Int, matchDay:Int, team:String, scored:Int, against:String, conceded:Int, points:Int) {

}

object PlayMatchEventApp extends App {

  lazy val producer: KafkaProducer[String, String] = Settings.producer
  val topic = if (args.length == 0) "football_matches" else args(0)

  val logLines = if(args.length > 1) args(1).toBoolean else false
  val delay = if(args.length > 2) args(2).toInt else 0

  val csvParser = new CSVParser()
  Files.list(Path.of("src/main/resources/epl"))
    .iterator().asScala
    //.filter(p => p.toString.contains("2012-13.csv"))
    .flatMap(f => Files.readAllLines(f).iterator().asScala)
    .filterNot(s => s.startsWith("Round,Date"))
    .map(csvParser.parseLine)
    .map(a => {
      val dt = LocalDate.parse(a(1), DateTimeFormatter.ofPattern("EEE MMM d yyyy", Locale.ENGLISH))
      val season = if (dt.getMonth.getValue > 7) dt.getYear else dt.getYear - 1
      FootballMatch(round = a(0).toInt, matchDate = dt, home = a(2), away = a(4), score = a(3), season = season)
    })
    .flatMap(fm => fm.toTeamData())
    .toList.groupBy(fm => (fm.season, fm.matchDay))
    .toList.sortBy(kv => kv._1)
    .foreach(matchDataGroup => {
      var offset = 0L
      matchDataGroup._2.foreach(matchData => {
        if (logLines) {
          println(s"${matchData.team}-${matchData.against}-${matchData.season}")
          println(JsonSupport.toJson(matchData))
        }
        val record = new ProducerRecord(topic,
          s"${matchData.team}-${matchData.against}-${matchData.season}", JsonSupport.toJson(matchData))
        val recordMetadata = producer.send(record).get()
        offset = recordMetadata.offset()
      })
      println(s"Round ${matchDataGroup._1} completed. offset ${offset}")
      if(matchDataGroup._1._2 == 38 && delay > 0) {
        Thread.sleep(delay)
      }
    })


}
