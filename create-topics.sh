docker exec -it kafka-0 kafka-topics --create \
--topic activity --partitions 1 --replication-factor 1 \
--if-not-exists --zookeeper zookeeper:2181
