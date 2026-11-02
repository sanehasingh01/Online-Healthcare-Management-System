FROM tomcat:10.1-jdk17-temurin AS build

WORKDIR /src
COPY . .

RUN mkdir -p /out/WEB-INF/classes /out/WEB-INF/lib \
    && javac -encoding UTF-8 \
        -cp "$CATALINA_HOME/lib/servlet-api.jar:lib/mysql-connector-j-26.7.0.jar" \
        -d /out/WEB-INF/classes *.java util/*.java \
    && cp *.html style.css /out/ \
    && cp WEB-INF/web.xml /out/WEB-INF/ \
    && cp lib/mysql-connector-j-26.7.0.jar /out/WEB-INF/lib/

FROM tomcat:10.1-jdk17-temurin

RUN rm -rf "$CATALINA_HOME/webapps/ROOT"
COPY --from=build /out/ "$CATALINA_HOME/webapps/ROOT/"
COPY docker/start-tomcat.sh /usr/local/bin/start-tomcat.sh
RUN chmod +x /usr/local/bin/start-tomcat.sh

EXPOSE 8080
CMD ["/usr/local/bin/start-tomcat.sh"]
