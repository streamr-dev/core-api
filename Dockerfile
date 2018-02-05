# Use official OpenJDK 7 as base image
FROM openjdk:7-jdk

# Set customizable env vars defaults.
# Set Grails version.
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
RUN curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.xz" -o "node-v$NODE_VERSION-linux-x64.tar.xz" \
    && tar -xJf "node-v$NODE_VERSION-linux-x64.tar.xz" -C /usr/local --strip-components=1 \
    && rm "node-v$NODE_VERSION-linux-x64.tar.xz"

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
CMD ["-Dstreamr.database.host=mysql -Dstreamr.kafka.bootstrap.servers=kafka:9092 -Dstreamr.redis.hosts=redis -Dstreamr.cassandra.hosts=cassandra run-app"]
