#!/bin/bash

MAVEN=$HOME/.m2/repository
CUP=$MAVEN/com/github/vbmacher/java-cup-runtime/11b-20160615/java-cup-runtime-11b-20160615.jar
CLASSPATH=$CUP:target/classes

java -cp $CLASSPATH pl.edu.mimuw.cloudatlas.interpreter.Main
