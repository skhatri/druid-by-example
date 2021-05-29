docker-compose down
for v in $(docker volume ls|grep druid|awk '{print $2}'); do docker volume rm $v; done

