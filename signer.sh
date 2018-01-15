#!/bin/bash

MAVEN=$HOME/.m2/repository
CUP=$MAVEN/com/github/vbmacher/java-cup-runtime/11b-20160615/java-cup-runtime-11b-20160615.jar
CLASSPATH=$CUP:target/classes

java -cp $CLASSPATH -Djava.rmi.server.hostname=localhost -Djava.security.policy=common.policy pl.edu.mimuw.cloudatlas.security.QuerySignerServer query_signer.private $1
