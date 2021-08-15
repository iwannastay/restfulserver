#!/bin/bash

mvn install

cp engine-server/target/engine-server-0.0.1-SNAPSHOT.jar /home/iws/Documents/restfulapi/restfulserver.jar

cd /home/iws/Documents/restfulapi

/bin/bash ./build.sh

docker login 10.16.17.92:8433 -u admin -p 123456

docker push 10.16.17.92:8433/public/restfulserver:latest


