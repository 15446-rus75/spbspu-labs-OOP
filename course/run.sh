#!/bin/bash

mvn clean package

#JVM_OPTS="-Dhttp.readTimeout=120 -Dhttp.connectTimeout=60 -Djava.net.preferIPv4Stack=true"
JVM_OPTS="-Djava.net.preferIPv4Stack=true"

java $JVM_OPTS -cp "target/api-aggregator-1.0-SNAPSHOT.jar:target/lib/*" main.Main \
  --mode auto \
  --apis chucknorris,zippopotam,randomuser \
  --format json \
  --output data.json \
  --max-threads 3 \
  --interval 60

java $JVM_OPTS -cp "target/api-aggregator-1.0-SNAPSHOT.jar:target/lib/*" main.Main "$@"
