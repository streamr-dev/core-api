# Use official Tomcat 7 runtime as base image
FROM tomcat:7.0-jre8-alpine

# Install dependencies
#   bash: required by wait_for_it.sh script
#   mysql: required for waiting for database dumps to be done
RUN apk update
RUN apk add --no-cache python3 bash mysql-client && \
    python3 -m ensurepip && \
    rm -r /usr/lib/python*/ensurepip && \
    pip3 install --upgrade pip setuptools && \
    if [ ! -e /usr/bin/pip ]; then ln -s pip3 /usr/bin/pip ; fi && \
    if [[ ! -e /usr/bin/python ]]; then ln -sf /usr/bin/python3 /usr/bin/python; fi && \
    rm -r /root/.cache
RUN pip install awscli --upgrade --user && \
     if [[ ! -e /usr/bin/aws ]]; then ln -sf ~/.local/bin/aws /usr/bin/aws; fi
RUN sed -i "s/port=\"8080\"/port=\"8081\"/g" /usr/local/tomcat/conf/server.xml
# Copy wait-for-it.sh script
COPY wait-for-it.sh /usr/local/tomcat/bin/wait-for-it.sh

# Copy pre-built war
COPY target/ROOT.war /usr/local/tomcat/webapps/streamr-core.war

# Copy entrypoint
COPY docker-entrypoint.sh docker-entrypoint.sh

#default enviroment variables
ENV REMOTE_SECRETS true
ENV BUCKET_NAME default_bucket
ENV APP_NAME default_app
ENV DB_HOST mysql
ENV DB_PASS password
ENV DB_USER root
ENV KAFKA_BOOTSTRAP_SERVERS kafka:9092
ENV UI_SERVER ws://127.0.0.1:8890/api/v1/ws
ENV HTTPS_API_SERVER http://127.0.0.1:8890/api/v1
ENV REDIS_HOSTS redis
ENV CASSANDRA_HOSTS cassandra

EXPOSE 8081

ENTRYPOINT ["sh","docker-entrypoint.sh"]

#Load Secrets
ENV CATALINA_OPTS -Dstreamr.database.user=${DB_USER}  \
    -Dstreamr.database.password=${DB_PASS}  \
    -Dstreamr.database.host=${DB_HOST} \
    -Dstreamr.kafka.bootstrap.servers=${DB_HOST} \
    -Dstreamr.ui.server=${UI_SERVER}  \
    -Dstreamr.http.api.server=${HTTPS_API_SERVER}  \
    -Dstreamr.redis.hosts=${REDIS_HOSTS}  \
    -Dstreamr.cassandra.hosts=${CASSANDRA_HOSTS}

# Wait for MySQL server, MySQL dumps, and Cassandra to be ready
CMD ["sh", "-c", "wait-for-it.sh mysql:3306 --timeout=120 && while ! mysql --user=root --host=mysql --password=password core_dev -e \"SELECT 1;\"; do echo 'waiting for db'; sleep 1; done && wait-for-it.sh cassandra:9042 --timeout=120 && catalina.sh run"]

