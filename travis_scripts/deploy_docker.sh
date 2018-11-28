#!/usr/bin/env bash
docker login -u "${DOCKER_USER}" -p "${DOCKER_PASS}"
if [ $1 = "staging" ]; then
    docker push $OWNER/$IMAGE_NAME:$TAG
elif [ $1 = "production" ]; then
    echo "to do"
fi