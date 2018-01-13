#!/bin/bash

MAVEN=$HOME/.m2/repository
JARS=$MAVEN/net/sourceforge/maven-jlex/maven-jlex-plugin/1.0/maven-jlex-plugin-1.0.jar:$MAVEN/org/apache/maven/maven-plugin-api/2.0/maven-plugin-api-2.0.jar:$MAVEN/com/github/vbmacher/java-cup-runtime/11b-20160615/java-cup-runtime-11b-20160615.jar:$MAVEN/org/ini4j/ini4j/0.5.4/ini4j-0.5.4.jar:$MAVEN/com/github/oshi/oshi-core/3.4.4/oshi-core-3.4.4.jar:$MAVEN/net/java/dev/jna/jna-platform/4.5.0/jna-platform-4.5.0.jar:$MAVEN/net/java/dev/jna/jna/4.5.0/jna-4.5.0.jar:$MAVEN/org/threeten/threetenbp/1.3.6/threetenbp-1.3.6.jar:$MAVEN/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar:$MAVEN/com/sparkjava/spark-core/2.1/spark-core-2.1.jar:$MAVEN/org/slf4j/slf4j-simple/1.7.7/slf4j-simple-1.7.7.jar:$MAVEN/org/eclipse/jetty/jetty-server/9.0.2.v20130417/jetty-server-9.0.2.v20130417.jar:$MAVEN/org/eclipse/jetty/orbit/javax.servlet/3.0.0.v201112011016/javax.servlet-3.0.0.v201112011016.jar:$MAVEN/org/eclipse/jetty/jetty-http/9.0.2.v20130417/jetty-http-9.0.2.v20130417.jar:$MAVEN/org/eclipse/jetty/jetty-util/9.0.2.v20130417/jetty-util-9.0.2.v20130417.jar:$MAVEN/org/eclipse/jetty/jetty-io/9.0.2.v20130417/jetty-io-9.0.2.v20130417.jar:$MAVEN/org/eclipse/jetty/jetty-webapp/9.0.2.v20130417/jetty-webapp-9.0.2.v20130417.jar:$MAVEN/org/eclipse/jetty/jetty-xml/9.0.2.v20130417/jetty-xml-9.0.2.v20130417.jar:$MAVEN/org/eclipse/jetty/jetty-servlet/9.0.2.v20130417/jetty-servlet-9.0.2.v20130417.jar:$MAVEN/org/eclipse/jetty/jetty-security/9.0.2.v20130417/jetty-security-9.0.2.v20130417.jar:$MAVEN/com/fasterxml/jackson/core/jackson-core/2.5.1/jackson-core-2.5.1.jar:$MAVEN/com/fasterxml/jackson/core/jackson-databind/2.5.1/jackson-databind-2.5.1.jar:$MAVEN/com/fasterxml/jackson/core/jackson-annotations/2.5.0/jackson-annotations-2.5.0.jar:$MAVEN/org/sql2o/sql2o/1.5.4/sql2o-1.5.4.jar:$MAVEN/org/postgresql/postgresql/9.4-1201-jdbc41/postgresql-9.4-1201-jdbc41.jar:$MAVEN/com/google/guava/guava/18.0/guava-18.0.jar:$MAVEN/com/google/code/gson/gson/2.8.0/gson-2.8.0.jar
CLASSPATH=$JARS:target/classes

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

java -cp $CLASSPATH -Djava.security.policy=common.policy pl.edu.mimuw.cloudatlas.client.Client $RMIHOST $RMIPORT
