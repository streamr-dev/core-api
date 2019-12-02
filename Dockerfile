# Use official Tomcat 7 runtime as base image
FROM tomcat:7.0-jre8-alpine

# Install dependencies
#   bash: required by wait_for_it.sh script
#   mysql-client: required for checking that mysql is up and running
RUN apk update
RUN apk add openjdk8
RUN apk add bash mysql-client
RUN sed -i 's/port="8080"/port="8081"/g' /usr/local/tomcat/conf/server.xml
COPY scripts/wait-for-it.sh /usr/local/tomcat/bin/wait-for-it.sh
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
ENV REDIS_HOSTS redis
ENV WS_SERVER ws://engine-and-editor/api/v1/ws
ENV HTTPS_API_SERVER http://engine-and-editor/api/v1
ENV STREAMR_URL http://localhost
ENV MARKETPLACE_URL http://localhost
ENV AWS_ACCESS_KEY_ID TODO
ENV AWS_SECRET_KEY TODO
ENV FILEUPLOAD_S3_BUCKET streamr-dev-public
ENV FILEUPLOAD_S3_REGION eu-west-1
ENV CPS_URL http://community-product:8085/communities/
ENV ETHEREUM_DEFAULT_NETWORK local
ENV ETHEREUM_NETWORKS_LOCAL http://ganache:8545

# Flags to pass to the JVM
ENV JAVA_OPTS \
	-Djava.awt.headless=true \
	-server \
	-Xms128M \
	-Xmx512M \
	-XX:+UseG1GC
ENV CATALINA_OPTS \
	-Dstreamr.database.user=$DB_USER \
	-Dstreamr.database.password=$DB_PASS \
	-Dstreamr.database.host=$DB_HOST \
	-Dstreamr.database.name=$DB_NAME \
	-Dgrails.mail.host=$SMTP_HOST \
	-Dgrails.mail.port=$SMTP_PORT \
	-Dstreamr.cassandra.hosts=$CASSANDRA_HOST \
	-Dstreamr.redis.hosts=$REDIS_HOSTS \
	-Dstreamr.api.websocket.url=$WS_SERVER \
	-Dstreamr.api.http.url=$HTTPS_API_SERVER  \
	-Dstreamr.url=$STREAMR_URL \
	-Daws.accessKeyId=$AWS_ACCESS_KEY_ID \
	-Daws.secretKey=$AWS_SECRET_KEY \
	-Dstreamr.fileUpload.s3.bucket=$FILEUPLOAD_S3_BUCKET \
	-Dstreamr.fileUpload.s3.region=$FILEUPLOAD_S3_REGION \
	-Dstreamr.cps.url=$CPS_URL \
	-Dstreamr.ethereum.defaultNetwork=$ETHEREUM_DEFAULT_NETWORK \
	-Dstreamr.ethereum.networks.local=$ETHEREUM_NETWORKS_LOCAL

EXPOSE 8081
# Wait for MySQL server and Cassandra to be ready
CMD ["sh", "-c", "wait-for-it.sh $DB_HOST:$DB_PORT --timeout=120 && while ! mysql --user=$DB_USER --host=$DB_HOST --password=$DB_PASS $DB_NAME -e \"SELECT 1;\"; do echo 'waiting for db'; sleep 1; done && wait-for-it.sh $CASSANDRA_HOST:$CASSANDRA_PORT --timeout=120 && catalina.sh run"]
