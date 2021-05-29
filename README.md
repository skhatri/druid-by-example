### Objective
Load EPL match data into druid to run analytical query as the data is stored

### start druid
Run druid quickstart and kafka
```
./start.sh
```

### create topics
create a kafka topic for epl football matches
```
docker exec -it kafka-0 kafka-topics --delete --topic football_matches --zookeeper zookeeper:2181

docker exec -it kafka-0 kafka-topics --create \
--topic football_matches --partitions 1 --replication-factor 1 \
--if-not-exists --zookeeper zookeeper:2181
```

### start supervisor
Add a kafka job
```
curl -X POST -H 'Content-Type: application/json' -d @epl-spec.json http://localhost:8081/druid/indexer/v1/supervisor
```

Check status of the data load at
```
curl http://localhost:8888/druid/indexer/v1/supervisor/football_matches/stats
```

### Load Data
Football Data collected from https://github.com/footballcsv/england

```
./gradlew runApp
```

### Query
Load up query console http://localhost:8888/unified-console.html#query
Start querying as data gets loaded
```
select * from football_matches
```

#### Who won the premier league for season 2011-12
```
select team, sum(points) as "total points", sum(scored) - sum(conceded) as "goal difference"
from football_matches
where season = 2011
group by team 
order by "total points" desc, "goal difference" desc
limit 4
```

#### Find each season's EPL winner
```
WITH 
  all_season_score as
    (select fm.season, fm.team, sum(fm.points) as total_points, sum(scored) - sum(conceded) as gd
    from football_matches fm
    group by fm.season, fm.team),
  season_top_points as   
    (select season, team, total_points, gd
    from all_season_score a
    where a.total_points = (select max(total_points) from all_season_score b where b.season = a.season)
    )
select * 
from season_top_points stp
where stp.gd = (select max(gd) from season_top_points stp2 where stp2.season = stp.season)
```

### Cleanup
Shutdown containers and remove volumes
```
./cleanup.sh
```
