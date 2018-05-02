docker login -u "${DOCKER_USER}" -p "${DOCKER_PASS}"

#Create Enviroment
docker run -i -t -e AWS_ACCESS_KEY_ID="${TERRAFORM_KEY_ID}" -e AWS_SECRET_ACCESS_KEY="${TERRAFORM_SECRET}" -e AWS_DEFAULT_REGION="eu-west-1" streamr/infra-docker-ee:latest make terraform-init terraform-stg apply-docker-clusters apply-docker-base-containers apply-docker-engine-editor apply-docker-data apply-docker-broker

#Destroy Enviroment
docker run -i -t -e AWS_ACCESS_KEY_ID="${TERRAFORM_KEY_ID}" -e AWS_SECRET_ACCESS_KEY="${TERRAFORM_SECRET}" -e AWS_DEFAULT_REGION="eu-west-1" streamr/infra-docker-ee:latest make terraform-init terraform-stg destroy-docker-base-containers destroy-docker-broker destroy-docker-data destroy-docker-engine-editor destroy-docker-clusters
