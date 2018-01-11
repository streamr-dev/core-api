# Use official OpenJDK 7 Alpine runtime as base image
FROM streamr/grails-docker

# Copy source code
COPY . /app

# Run Grails refresh-dependencies command to pre-download dependencies but not create unnecessary build files or
# artifacts.
RUN grails refresh-dependencies
RUN grails compile

# Set Default Behavior
ENTRYPOINT ["grails"]
CMD ["-Dstreamr.database.host=mysql -Dstreamr.kafka.bootstrap.servers=kafka:9092 -Dstreamr.redis.hosts=redis -Dstreamr.cassandra.hosts=cassandra run-app"]
