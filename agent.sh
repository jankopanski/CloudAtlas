#!/bin/bash

MAVEN=$HOME/.m2/repository
CUP=$MAVEN/com/github/vbmacher/java-cup-runtime/11b-20160615/java-cup-runtime-11b-20160615.jar
JSON=$MAVEN/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar
CLASSPATH=$CUP:$JSON:target/classes

java -cp $CLASSPATH -Djava.rmi.server.hostname=$RMIHOST -Djava.security.policy=common.policy pl.edu.mimuw.cloudatlas.agent.Main query_signer.public $1
