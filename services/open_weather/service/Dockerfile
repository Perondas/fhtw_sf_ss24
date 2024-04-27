FROM gradle:jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM amazoncorretto:21

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/open_scraper.jar

ENTRYPOINT ["java", "-jar", "/app/open_scraper.jar"]
