docker login -u "${DOCKER_USER}" -p "${DOCKER_PASS}"
docker run -i -t -e AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}" -e AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}" -e AWS_DEFAULT_REGION="eu-west-1" streamr/infra-docker-ee init && terraform apply -target=module.docker-broker -var-file=docker-broker/conf/eu-west-1-stg-docker-broker.tfvars
