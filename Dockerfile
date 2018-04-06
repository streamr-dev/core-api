# Use official OpenJDK 8 as base image
FROM openjdk:8-jdk-alpine

# Set customizable env vars defaults.
# Set Grails version.
RUN apk update
RUN apk add curl git

ENV GRAILS_VERSION 2.5.6
ENV NODE_VERSION 8.9.4

# Install Grails
WORKDIR /usr/lib/jvm
RUN wget https://github.com/grails/grails-core/releases/download/v$GRAILS_VERSION/grails-$GRAILS_VERSION.zip && \
    unzip grails-$GRAILS_VERSION.zip && \
    rm -rf grails-$GRAILS_VERSION.zip && \
    ln -s grails-$GRAILS_VERSION grails

# Setup Grails path
ENV GRAILS_HOME /usr/lib/jvm/grails
ENV PATH $GRAILS_HOME/bin:$PATH

# Download and Install Node
RUN apk add --update nodejs nodejs-npm

# Confirm node version
RUN node --version
RUN npm --version

# Create App Directory
RUN mkdir /app

# Set Workdir
WORKDIR /app

# Copy source code
COPY . /app

# Run Grails refresh-dependencies command to pre-download dependencies but not create unnecessary build files or
# artifacts.
RUN grails refresh-dependencies
RUN grails compile

RUN npm install

# Set Default Behavior
ENTRYPOINT ["grails"]
CMD ["-Dstreamr.database.host=localhost -Dstreamr.kafka.bootstrap.servers=localhost:9092 -Dstreamr.redis.hosts=localhost -Dstreamr.cassandra.hosts=localhost run-app"]
