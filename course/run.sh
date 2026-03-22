#!/bin/bash

mvn clean package

java -jar target/data-aggregator-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --mode auto \
  --apis chucknorris,zippopotam \
  --format json \
  --output data.json

java -jar target/data-aggregator-1.0-SNAPSHOT-jar-with-dependencies.jar "$@"
