#!/bin/bash
set -e
if $REMOTE_SECRETS; then
   #fetches remote secrets encrypted on AWS
   echo "Configs:"
   echo "$BUCKET_NAME"
   echo "$APP_NAME"
   aws s3 cp s3://$BUCKET_NAME/$APP_NAME/configs/secrets.enc /tmp
   $(aws kms decrypt --region eu-west-1 --ciphertext-blob fileb:///tmp/secrets.enc --output text --query Plaintext | base64 -d)
   rm /tmp/secrets.enc
fi