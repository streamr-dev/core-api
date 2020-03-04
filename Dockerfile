# Use official Tomcat 7 runtime as base image
FROM tomcat:7.0-jre8-alpine

# Install dependencies
#   bash: required by wait_for_it.sh script
#   mysql-client: required for checking that mysql is up and running
#   curl: container healthcheck
RUN apk update && apk add \
    openjdk8 \
    bash \
    mysql-client \
    curl \
    && rm -rf /var/cache/apk/*
RUN sed -i 's/port="8080"/port="8081"/g' /usr/local/tomcat/conf/server.xml
COPY scripts/wait-for-it.sh scripts/entrypoint.sh /usr/local/tomcat/bin/
COPY target/ROOT.war /usr/local/tomcat/webapps/streamr-core.war

# Default values for ENV variables
ENV DB_USER root
ENV DB_PASS password
ENV DB_HOST mysql
ENV DB_PORT 3306
ENV DB_NAME core_test
ENV SMTP_HOST smtp
ENV SMTP_PORT 25
ENV CASSANDRA_HOST cassandra
ENV CASSANDRA_PORT 9042
ENV CASSANDRA_KEYSPACE streamr_dev
ENV REDIS_HOSTS redis
ENV WS_SERVER ws://10.200.10.1/api/v1/ws
ENV HTTPS_API_SERVER http://10.200.10.1/api/v1
ENV STREAMR_URL http://localhost
ENV MARKETPLACE_URL http://localhost
ENV AWS_ACCESS_KEY_ID TODO
ENV AWS_SECRET_KEY TODO
ENV FILEUPLOAD_S3_BUCKET streamr-dev-public
ENV FILEUPLOAD_S3_REGION eu-west-1
ENV CPS_URL http://10.200.10.1:8085/dataunions/
ENV ETHEREUM_DEFAULT_NETWORK local
ENV ETHEREUM_NETWORKS_LOCAL http://10.200.10.1:8545
ENV ETHEREUM_SERVER_URL http://10.200.10.1:8545
ENV STREAMR_ENCRYPTION_PASSWORD password

# Flags to pass to the JVM
ENV JAVA_OPTS \
	-Djava.awt.headless=true \
	-server \
	-Xms128M \
	-Xmx512M \
	-XX:+UseG1GC \
    -Dcom.sun.management.jmxremote=true \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.port=9090 \
    -Dcom.sun.management.jmxremote.ssl=false

HEALTHCHECK --interval=5m --timeout=3s --start-period=100s --retries=3 CMD /usr/bin/curl -s http://localhost:8081/streamr-core/api/v1/products || exit 1
EXPOSE 8081
CMD ["/usr/local/tomcat/bin/entrypoint.sh"]

