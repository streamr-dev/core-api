# Use official Tomcat 7 runtime as base image
FROM tomcat:7.0-jre8-alpine

# Install dependencies
#   bash: required by wait_for_it.sh script
#   mysql: required for waiting for database dumps to be done
RUN apk update
RUN apk add bash mysql-client

# Copy wait-for-it.sh script
COPY wait-for-it.sh /usr/local/tomcat/bin/wait-for-it.sh

# Copy pre-built unifina-core.war into container as unifina-core.war
COPY target/unifina-core.war /usr/local/tomcat/webapps/unifina-core.war

ENV CATALINA_OPTS -Dstreamr.database.user=root \
    -Dstreamr.database.password=password \
    -Dstreamr.database.host=mysql \
    -Dstreamr.kafka.bootstrap.servers=kafka:9092 \
    -Dstreamr.ui.server=ws://127.0.0.1:8890/api/v1/ws \
    -Dstreamr.http.api.server=http://127.0.0.1:8890/api/v1 \
    -Dstreamr.redis.hosts=redis \
    -Dstreamr.cassandra.hosts=cassandra

# Wait for MySQL server, MySQL dumps, and Cassandra to be ready
CMD ["sh", "-c", "wait-for-it.sh mysql:3306 --timeout=120 && while ! mysql --user=root --host=mysql --password=password streamr_dev -e \"SELECT 1;\"; do echo 'waiting for db'; sleep 1; done && wait-for-it.sh cassandra:9042 --timeout=120 && catalina.sh run"]
