#!/bin/bash

set -e

# Script for logging in to Docker service and to tag and push docker images
if [ -z "$1" ]; then
	echo "deploy_docker.sh: docker build target not defined error: use 'dev' or 'production'" 1>&2
	exit 1
fi

docker login -u "$DOCKER_USER" -p "$DOCKER_PASS"

case "$1" in
	dev)
		# If the build is a cron build then it should tag and push a nightly build
		# but if it is not a cronjob then it is just another dev tag and push
		if [ "$TRAVIS_EVENT_TYPE" == "cron" ]; then
			nightly_build="nightly-$(date '+%Y-%m-%d')"
			docker tag "$OWNER/$IMAGE_NAME:local" "$OWNER/$IMAGE_NAME:$nightly_build"
			docker tag "$OWNER/$IMAGE_NAME:local" "$OWNER/$IMAGE_NAME:nightly"
			docker push "$OWNER/$IMAGE_NAME:$nightly_build"
			docker push "$OWNER/$IMAGE_NAME:nightly"
			exit 0
		fi
		docker tag "$OWNER/$IMAGE_NAME:local" "$OWNER/$IMAGE_NAME:dev"
		docker push "$OWNER/$IMAGE_NAME:dev"
		;;
	production)
		docker tag "$OWNER/$IMAGE_NAME:local" "$OWNER/$IMAGE_NAME:$TRAVIS_TAG"
		docker tag "$OWNER/$IMAGE_NAME:local" "$OWNER/$IMAGE_NAME:latest"
		docker push "$OWNER/$IMAGE_NAME:$TRAVIS_TAG"
		docker push "$OWNER/$IMAGE_NAME:latest"
		;;
	*)
		echo "deploy_docker.sh: unknown build target '$1': use 'dev' or 'production'" 1>&2
		exit 1
		;;
esac

