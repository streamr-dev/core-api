#!/bin/bash

CATALINA_OPTS="\
	-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true \
	-Dstreamr.database.user=$DB_USER \
	-Dstreamr.database.password=$DB_PASS \
	-Dstreamr.database.host=$DB_HOST \
	-Dstreamr.database.name=$DB_NAME \
	-Dgrails.mail.host=$SMTP_HOST \
	-Dgrails.mail.port=$SMTP_PORT \
	-Dstreamr.redis.hosts=$REDIS_HOSTS \
	-Dstreamr.api.websocket.url=$WS_SERVER \
	-Dstreamr.api.http.url=$HTTPS_API_SERVER \
	-Dstreamr.url=$STREAMR_URL \
	-Daws.accessKeyId=$AWS_ACCESS_KEY_ID \
	-Daws.secretKey=$AWS_SECRET_KEY \
	-Dstreamr.fileUpload.s3.bucket=$FILEUPLOAD_S3_BUCKET \
	-Dstreamr.fileUpload.s3.region=$FILEUPLOAD_S3_REGION \
	-Dstreamr.cps.url=$CPS_URL \
	-Dstreamr.dataunion.mainnet.factory.address=$DATAUNION_MAINNET_FACTORY_ADDRESS \
	-Dstreamr.dataunion.sidechain.factory.address=$DATAUNION_SIDECHAIN_FACTORY_ADDRESS \
	-Dstreamr.ethereum.defaultNetwork=$ETHEREUM_DEFAULT_NETWORK \
	-Dstreamr.ethereum.networks.local=$ETHEREUM_NETWORKS_LOCAL \
	-Dstreamr.ethereum.networks.sidechain=$ETHEREUM_NETWORKS_SIDECHAIN \
	-Dstreamr.ethereum.nodePrivateKey=$ETHEREUM_NODE_PRIVATE_KEY \
	-Dstreamr.ethereum.ensRegistryContractAddress=$ETHEREUM_ENS_REGISTRY_CONTRACT_ADDRESS \
	-Dstreamr.encryption.password=$STREAMR_ENCRYPTION_PASSWORD \
"
wait-for-it.sh "$DB_HOST:$DB_PORT" --timeout=300 \
	&& while ! mysql --user="$DB_USER" --host="$DB_HOST" --password="$DB_PASS" "$DB_NAME" -e "SELECT 1;" 1>/dev/null; do echo "waiting for db"; sleep 1; done \
	&& CATALINA_OPTS="$CATALINA_OPTS" catalina.sh run

