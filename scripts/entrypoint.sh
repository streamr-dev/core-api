#!/bin/bash

CATALINA_OPTS="\
	-Dstreamr.database.user=$DB_USER \
	-Dstreamr.database.password=$DB_PASS \
	-Dstreamr.database.host=$DB_HOST \
	-Dstreamr.database.name=$DB_NAME \
	-Dgrails.mail.host=$SMTP_HOST \
	-Dgrails.mail.port=$SMTP_PORT \
	-Dstreamr.cassandra.hosts=$CASSANDRA_HOST \
	-Dstreamr.cassandra.keySpace=$CASSANDRA_KEYSPACE \
	-Dstreamr.redis.hosts=$REDIS_HOSTS \
	-Dstreamr.api.websocket.url=$WS_SERVER \
	-Dstreamr.api.http.url=$HTTPS_API_SERVER \
	-Dstreamr.url=$STREAMR_URL \
	-Daws.accessKeyId=$AWS_ACCESS_KEY_ID \
	-Daws.secretKey=$AWS_SECRET_KEY \
	-Dstreamr.fileUpload.s3.bucket=$FILEUPLOAD_S3_BUCKET \
	-Dstreamr.fileUpload.s3.region=$FILEUPLOAD_S3_REGION \
	-Dstreamr.cps.url=$CPS_URL \
	-Dstreamr.ethereum.defaultNetwork=$ETHEREUM_DEFAULT_NETWORK \
	-Dstreamr.ethereum.networks.local=$ETHEREUM_NETWORKS_LOCAL \
	-Dstreamr.ethereum.nodePrivateKey=$ETHEREUM_NODE_PRIVATE_KEY \
	-Dstreamr.encryption.password=$STREAMR_ENCRYPTION_PASSWORD \
"
wait-for-it.sh "$DB_HOST:$DB_PORT" --timeout=120 \
	&& while ! mysql --user="$DB_USER" --host="$DB_HOST" --password="$DB_PASS" "$DB_NAME" -e "SELECT 1;" 1>/dev/null; do echo "waiting for db"; sleep 1; done \
	&& wait-for-it.sh "$CASSANDRA_HOST:$CASSANDRA_PORT" --timeout=120 && \
	CATALINA_OPTS="$CATALINA_OPTS" catalina.sh run

