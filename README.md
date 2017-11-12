# CloudAtlas

Create project  
mvn archetype:generate -DgroupId=pl.edu.mimuw.cloudatlas -DartifactId=CloudAtlas -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

Compile project  
mvn compile

Build project (sources + tests)  
mvn package

Run App  
cd target/classes  
java pl.edu.mimuw.cloudatlas.App  
Or  
java -cp target/classes pl.edu.mimuw.cloudatlas.App  

Tests  
./run_tests

Running Fetcher in IntelliJ Idea  
Name: Fetcher  
Main class: pl.edu.mimuw.cloudatlas.fetcher.FetcherServer  
VM options: -Djava.rmi.server.hostname=localhost -Djava.security.policy=common.policy  
Program arguments: fetcher.ini  
Working directory: /home/user/IdeaProjects/CloudAtlas
