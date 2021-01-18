FROM streamr/grails-builder:v0.0.3 AS builder
# GRAILS_WAR_ENV argument must be 'prod' or 'test'. Default is 'prod'.
ARG GRAILS_WAR_ENV
ENV GRAILS_WAR_ENV=${GRAILS_WAR_ENV:-prod}
COPY . /src/engine-and-editor
WORKDIR /src/engine-and-editor
RUN grails -verbose -stacktrace -non-interactive -plain-output $GRAILS_WAR_ENV war


FROM tomcat:7.0.106-jdk8-openjdk-buster
# bash is required by wait_for_it.sh script and provided by base image
# curl is required for container healthcheck
# mysql-client is required by entrypoint.sh
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get -y --no-install-recommends install \
	curl \
	default-mysql-client
COPY src/conf/tomcat-server.xml /usr/local/tomcat/conf/server.xml
COPY scripts/wait-for-it.sh scripts/entrypoint.sh /usr/local/tomcat/bin/
COPY --from=builder /src/engine-and-editor/target/ROOT.war /usr/local/tomcat/webapps/streamr-core.war

# Default values for ENV variables
ENV DB_USER root
ENV DB_PASS password
ENV DB_HOST mysql
ENV DB_PORT 3306
ENV DB_NAME core_test
ENV SMTP_HOST smtp
ENV SMTP_PORT 25
ENV REDIS_HOSTS redis
ENV WS_SERVER ws://10.200.10.1/api/v1/ws
ENV HTTPS_API_SERVER http://10.200.10.1/api/v1
ENV STREAMR_URL http://localhost
ENV AWS_ACCESS_KEY_ID TODO
ENV AWS_SECRET_KEY TODO
ENV FILEUPLOAD_S3_BUCKET streamr-dev-public
ENV FILEUPLOAD_S3_REGION eu-west-1
ENV CPS_URL http://10.200.10.1:8085/dataunions/
ENV ETHEREUM_DEFAULT_NETWORK local
ENV ETHEREUM_NETWORKS_LOCAL http://10.200.10.1:8545
ENV ETHEREUM_NETWORKS_SIDECHAIN http://10.200.10.1:8546
ENV ETHEREUM_SERVER_URL http://10.200.10.1:8545
ENV STREAMR_ENCRYPTION_PASSWORD password
ENV DATAUNION_MAINNET_FACTORY_ADDRESS 0x5E959e5d5F3813bE5c6CeA996a286F734cc9593b
ENV DATAUNION_SIDECHAIN_FACTORY_ADDRESS 0x4081B7e107E59af8E82756F96C751174590989FE

# Flags to pass to the JVM
ENV JAVA_OPTS \
	-Djava.awt.headless=true \
	-server \
	-Xms256M \
	-Xmx1024M \
	-XX:+UseG1GC \
	-Dcom.sun.management.jmxremote=true \
	-Dcom.sun.management.jmxremote.authenticate=false \
	-Dcom.sun.management.jmxremote.port=9090 \
	-Dcom.sun.management.jmxremote.ssl=false

HEALTHCHECK --interval=5m --timeout=3s --start-period=100s --retries=3 CMD /usr/bin/curl -s http://localhost:8081/streamr-core/api/v1/products || exit 1
EXPOSE 8081
CMD ["/usr/local/tomcat/bin/entrypoint.sh"]

