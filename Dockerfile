FROM eclipse-temurin:17-jdk-alpine

WORKDIR /srv/mtserver-bot

COPY target/discordbot-*.jar discordbot.jar
ENTRYPOINT [ "java", "-jar", "discordbot.jar" ]
