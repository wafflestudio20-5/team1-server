FROM amazoncorretto:17-alpine
VOLUME /tmp
COPY build/libs/*.jar app.jar
ARG PROFILES
RUN echo $PROFILES
ENV spring.profiles.active=$PROFILES
ENTRYPOINT ["java", "-jar", "/app.jar"]