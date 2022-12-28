FROM amazoncorretto:17-alpine
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar", "--spring.config.additional-location=optional:classpath:application-local.yml"]