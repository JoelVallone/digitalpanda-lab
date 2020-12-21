# digitalpanda-backend
Laboratory for the backend system

#API endpoints
- /sensor/keys
- /sensor?location=server-room&type=TEMPERATURE

#Misc
Before running commands:
> export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64;

Publish to maven local : 
> mvn install -DskipTests

Run test with java 8:  
> mvn clean test

Run local server : 
> java -jar -Dspring.profiles.active=local.prod ./target/digitalpanda-backend-0.1.0.jar
