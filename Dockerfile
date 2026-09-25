# Build WAR
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xmx768m"
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests || true
COPY src ./src
RUN mvn -B -DskipTests package

# Run on Tomcat (Render sets PORT)
FROM tomcat:9.0-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/InBank.war /usr/local/tomcat/webapps/InBank.war
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN sed -i 's/\r$//' /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/docker-entrypoint.sh"]
