docker login -u "${DOCKER_USER}" -p "${DOCKER_PASS}"
docker run -i -t -e AWS_ACCESS_KEY_ID="${TERRAFORM_KEY_ID}" -e AWS_SECRET_ACCESS_KEY="${TERRAFORM_SECRET}" -e AWS_DEFAULT_REGION="eu-west-1" streamr/infra-docker-ee:latest make terraform-init terraform-stg
