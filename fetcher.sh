#!/bin/bash

MAVEN=$HOME/.m2/repository
INI4J=$MAVEN/org/ini4j/ini4j/0.5.4/ini4j-0.5.4.jar
OSHI=$MAVEN/com/github/oshi/oshi-core/3.4.4/oshi-core-3.4.4.jar
JNA=$MAVEN/net/java/dev/jna/jna/4.5.0/jna-4.5.0.jar
SLF4JAPI=$MAVEN/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar
SLF4J=$MAVEN/org/slf4j/slf4j-simple/1.7.7/slf4j-simple-1.7.7.jar
THREETEN=$MAVEN/org/threeten/threetenbp/1.3.6/threetenbp-1.3.6.jar
CLASSPATH=$INI4J:$OSHI:$JNAP:$JNA:$THREETEN:$SLF4JAPI:$SLF4J:target/classes

java -cp $CLASSPATH -Djava.rmi.server.hostname=localhost -Djava.security.policy=common.policy pl.edu.mimuw.cloudatlas.fetcher.Fetcher fetcher.ini $1
