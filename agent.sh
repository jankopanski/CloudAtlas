#!/bin/bash

java -cp target/classes -Djava.rmi.server.hostname=localhost -Djava.security.policy=common.policy pl.edu.mimuw.cloudatlas.agent.Main &
