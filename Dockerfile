# docker build . -t ebics-java-client
# docker run -p 8080:8080 ebics-java-client
# docker run -p 8080:8080 -v ./conf:/app/conf -e EWC_CONFIG_HOME=/app/conf ebics-java-client

FROM maven:3-jdk-8 as build
#16 as build
RUN mkdir app
COPY ./ebics-dbmodel /app/ebics-dbmodel
COPY ./ebics-java-lib /app/ebics-java-lib
COPY ./ebics-rest-api /app/ebics-rest-api
COPY ./ebics-web-ui /app/ebics-web-ui
COPY ./pom.xml /app/
WORKDIR /app
RUN mvn clean install -DskipTests
FROM openjdk:8-alpine
#16-alpine
#COPY ./libpatch/local_policy.jar ./libpatch/US_export_policy.jar /opt/jdk/jre/lib/security/
RUN mkdir /app
COPY --from=build /app/ebics-rest-api/target/ebics*.war /app/
WORKDIR /app
#remove version form jar files in container and note the used version
EXPOSE 8080
RUN FN=`(ls ebics-rest-api-*[0-9].war | head -1)`;  echo $FN;  mv $FN ebics-rest-api.war;  touch $FN.version
ENTRYPOINT ["java","-jar","ebics-rest-api.war"]


