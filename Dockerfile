# Builds the existing Maven WAR and deploys it to Tomcat 11.0.15 on Java 17.
# Local development still uses: mvn compile exec:java

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
COPY sql ./sql
RUN mvn -B -DskipTests package

FROM tomcat:11.0.15-jre17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/target/irctc-admin.war /usr/local/tomcat/webapps/ROOT.war
COPY docker/render-entrypoint.sh /usr/local/bin/irctc-entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/irctc-entrypoint.sh && chmod +x /usr/local/bin/irctc-entrypoint.sh
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/irctc-entrypoint.sh"]
