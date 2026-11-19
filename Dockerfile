# docker build . -t ebics-java-client
# docker run -p 8080:8080 ebics-java-client
# docker run -p 8080:8080 -v ./conf:/app/conf -e SPRING_CONFIG_ADDITIONAL_LOCATION=/app/conf ebics-java-client

FROM docker.io/maven:3.10-amazoncorretto-21 as build

RUN mkdir app
COPY . /app/

WORKDIR /app
RUN mvn clean install -DskipTests
# pinned to the build fixing CVE-2026-114938 (Java4Shell), digest-verified
FROM docker.io/library/amazoncorretto:21.0.13-alpine-jdk@sha256:600e99e39da14b6a909027a924136c423e9334cb7347a8a77d5908bdaa3011b7

RUN mkdir /app
COPY --from=build /app/ebics-rest-api/target/ebics*.war /app/
WORKDIR /app
#remove version form jar files in container and note the used version
EXPOSE 8080
EXPOSE 8443
RUN FN=`(ls ebics-rest-api-*[0-9].war | head -1)`;  echo $FN;  mv $FN ebics-rest-api.war;  touch $FN.version
ENTRYPOINT ["java","-jar","ebics-rest-api.war"]


