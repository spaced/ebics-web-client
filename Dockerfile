# docker build . -t ebics-java-client
# docker run -p 8080:8080 ebics-java-client
# docker run -p 8080:8080 -v ./conf:/app/conf -e SPRING_CONFIG_ADDITIONAL_LOCATION=/app/conf ebics-java-client

FROM maven:3 as build

RUN mkdir app
COPY . /app/

WORKDIR /app
RUN mvn clean install -DskipTests
FROM amazoncorretto:21-alpine-jdk

RUN mkdir /app
COPY --from=build /app/ebics-rest-api/target/ebics*.war /app/
WORKDIR /app
#remove version form jar files in container and note the used version
EXPOSE 8080
EXPOSE 8443
RUN FN=`(ls ebics-rest-api-*[0-9].war | head -1)`;  echo $FN;  mv $FN ebics-rest-api.war;  touch $FN.version
ENTRYPOINT ["java","-jar","ebics-rest-api.war"]


