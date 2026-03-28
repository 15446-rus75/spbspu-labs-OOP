#!/bin/bash

mvn clean package

java -cp "target/api-aggregator-1.0-SNAPSHOT.jar:target/lib/*" main.Main \
  --mode auto \
  --apis chucknorris,zippopotam,randomuser \
  --format json \
  --output data.json

java -cp "target/api-aggregator-1.0-SNAPSHOT.jar:target/lib/*" main.Main "$@"
