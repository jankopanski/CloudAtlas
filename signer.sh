#!/bin/bash

MAVEN=$HOME/.m2/repository
CUP=$MAVEN/com/github/vbmacher/java-cup-runtime/11b-20160615/java-cup-runtime-11b-20160615.jar
CLASSPATH=$CUP:target/classes

if [ $# -eq 2 ]; then
	RMIHOST=$1
	RMIPORT=$2
else
	RMIPORT=$1
	if [ -n $HOSTNAME ]; then
		RMIHOST=$HOSTNAME
	elif [ -n $HOST ]; then
		RMIHOST=$HOST
	else
		RMIHOST="localhost"
	fi
fi

java -cp $CLASSPATH -Djava.rmi.server.hostname=$RMIHOST -Djava.security.policy=common.policy pl.edu.mimuw.cloudatlas.security.QuerySignerServer query_signer.private $RMIHOST $RMIPORT
