FROM streamr/grails-builder:v0.0.6 AS builder
# GRAILS_WAR_ENV argument must be 'prod' or 'test'. Default is 'prod'.
ARG GRAILS_WAR_ENV
ENV GRAILS_WAR_ENV=${GRAILS_WAR_ENV:-prod}
COPY . /src/core-api
WORKDIR /src/core-api
RUN grails -non-interactive -plain-output $GRAILS_WAR_ENV war


FROM tomcat:9.0.50-jdk8-openjdk-slim-buster
# bash is required by wait_for_it.sh script and provided by base image
# curl is required for container healthcheck
# mysql-client is required by entrypoint.sh
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get -y --no-install-recommends install \
	curl \
	default-mysql-client \
	&& apt-get clean \
	&& rm -rf /var/lib/apt/lists/*
COPY src/conf/tomcat-server.xml /usr/local/tomcat/conf/server.xml
COPY scripts/wait-for-it.sh scripts/entrypoint.sh /usr/local/tomcat/bin/
COPY --from=builder /src/core-api/target/ROOT.war /usr/local/tomcat/webapps/streamr-core.war

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
ENV DATAUNION_MAINNET_FACTORY_ADDRESS 0x4bbcBeFBEC587f6C4AF9AF9B48847caEa1Fe81dA
ENV DATAUNION_SIDECHAIN_FACTORY_ADDRESS 0x4A4c4759eb3b7ABee079f832850cD3D0dC48D927

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

