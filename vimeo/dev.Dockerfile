FROM openjdk:11

CMD mkdirs /app/files

ADD ./build/libs/vimeo.jar /app/vimeo.jar
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-XX:+UseSerialGC", "-Xss512k", "-Xmx128M","-jar", "/app/vimeo.jar"]
EXPOSE 8780
