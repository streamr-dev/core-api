# Use official OpenJDK 7 Alpine runtime as base image
FROM streamr/grails-docker

# Install dependencies
#   mysql: required for waiting for database dumps to be done
#RUN apk update
#RUN apk add mysql-client

# Copy source code
COPY . /app

# Run Grails refresh-dependencies command to pre-download dependencies but not create unnecessary build files or
# artifacts.
RUN grails refresh-dependencies
RUN grails compile

# Set Default Behavior
ENTRYPOINT ["grails"]
CMD ["-Dstreamr.database.host=mysql -Dstreamr.kafka.bootstrap.servers=kafka:9092 -Dstreamr.redis.hosts=redis -Dstreamr.cassandra.hosts=cassandra run-app"]

# Wait for MySQL server, MySQL dumps, and Cassandra to be ready
#CMD ["sh", "-c", "wait-for-it.sh mysql:3306 --timeout=120 && while ! mysql --user=root --host=mysql --password=password streamr_dev -e \"SELECT 1;\"; do echo 'waiting for db'; sleep 1; done && wait-for-it.sh cassandra:9042 --timeout=120 && catalina.sh run"]