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

### Cleanup
Shutdown containers and remove volumes
```
./cleanup.sh
```
