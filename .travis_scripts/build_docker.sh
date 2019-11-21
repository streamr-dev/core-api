#!/bin/bash

# Script for logging in to Docker service and to build and push docker images
if [ -z "$1" ]; then
	echo "build_docker.sh: docker build target not defined error: use 'dev' or 'production'" 1>&2
	exit 1
fi

docker login -u "$DOCKER_USER" -p "$DOCKER_PASS"

case "$1" in
	dev)
		# If the build is a cron build then it should tag and push a nightly build
		# but if it is not a cronjob then it is just another dev tag and push
		if [ "$TRAVIS_EVENT_TYPE" == "cron" ]; then
			# The script detects that there is a cron job through the variable
			# TRAVIS_EVENT_TYPE which will be 'cron' if the build is triggered by
			# a cron job
			echo "Tag Nightly"
			nightly_build=nightly-$(date '+%Y-%m-%d')
			docker tag "$OWNER/$IMAGE_NAME:taggit" "$OWNER/$IMAGE_NAME:$nightly_build"
			docker tag "$OWNER/$IMAGE_NAME:taggit" "$OWNER/$IMAGE_NAME:nightly"
			docker push "$OWNER/$IMAGE_NAME:$nightly_build"
			docker push "$OWNER/$IMAGE_NAME:nightly"
		else
			echo "Tag dev"
			docker tag "$OWNER/$IMAGE_NAME:taggit" "$OWNER/$IMAGE_NAME:$1"
			docker push "$OWNER/$IMAGE_NAME:$1"
		fi
		;;
	production)
		echo "Tag Production latest/tag"
		docker tag "$OWNER/$IMAGE_NAME:taggit" "$OWNER/$IMAGE_NAME:$TRAVIS_TAG"
		docker tag "$OWNER/$IMAGE_NAME:taggit" "$OWNER/$IMAGE_NAME:latest"
		# Push Production
		docker push "$OWNER/$IMAGE_NAME:$TRAVIS_TAG"
		docker push "$OWNER/$IMAGE_NAME:latest"
		;;
	*)
		echo "build_docker.sh: unknown build target '$1': use 'dev' or 'production'" 1>&2
		exit 1
		;;
esac
exit 0

