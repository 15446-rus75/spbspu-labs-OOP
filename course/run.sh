#!/bin/bash

mvn clean package

JVM_OPTS="-Djava.net.preferIPv4Stack=true"

java $JVM_OPTS -cp "target/api-aggregator-1.0-SNAPSHOT.jar:target/lib/*" main.Main \
  --mode auto \
  --apis chucknorris,chucknorris,randomuser \
  --format json \
  --output data.json \
  --max-threads 3 \
  --interval 0

#java $JVM_OPTS -cp "target/api-aggregator-1.0-SNAPSHOT.jar:target/lib/*" main.Main "$@"
