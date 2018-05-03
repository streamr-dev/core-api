docker login -u "${DOCKER_USER}" -p "${DOCKER_PASS}"

#Create Enviroment
docker run -i -t -e AWS_ACCESS_KEY_ID="${TERRAFORM_KEY_ID}" -e AWS_SECRET_ACCESS_KEY="${TERRAFORM_SECRET}" -e AWS_DEFAULT_REGION="eu-west-1" streamr/infra-docker-ee:latest make terraform-init terraform-stg apply-docker-clusters apply-docker-base-containers apply-docker-engine-editor apply-docker-data apply-docker-broker


docker run -i -t -e AWS_ACCESS_KEY_ID="${TERRAFORM_KEY_ID}" -e AWS_SECRET_ACCESS_KEY="${TERRAFORM_SECRET}" -e AWS_DEFAULT_REGION="eu-west-1" streamr/infra-docker-ee:latest make terraform-init terraform-stg apply-docker-engine-editor


counter=0
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' streamr-public-lb-stg-1321633029.eu-west-1.elb.amazonaws.com:8081/streamr-core/login/auth)" != "200" ]]; do
 echo "Waiting for Engine and Editor"
 counter=$((counter+1))
 echo $counter
  sleep 60;
 if [ $counter -ge 5 ]; then
 exit 0
 fi
 done

docker run -i -t -e AWS_ACCESS_KEY_ID="${TERRAFORM_KEY_ID}" -e AWS_SECRET_ACCESS_KEY="${TERRAFORM_SECRET}" -e AWS_DEFAULT_REGION="eu-west-1" streamr/infra-docker-ee:latest make terraform-init terraform-stg apply-docker-data

docker run -i -t -e AWS_ACCESS_KEY_ID="${TERRAFORM_KEY_ID}" -e AWS_SECRET_ACCESS_KEY="${TERRAFORM_SECRET}" -e AWS_DEFAULT_REGION="eu-west-1" streamr/infra-docker-ee:latest make terraform-init terraform-stg apply-docker-broker

#docker run -i -t -e AWS_ACCESS_KEY_ID="${TERRAFORM_KEY_ID}" -e AWS_SECRET_ACCESS_KEY="${TERRAFORM_SECRET}" -e AWS_DEFAULT_REGION="eu-west-1" streamr/infra-docker-ee:latest make terraform-init terraform-stg destroy-docker-base-containers destroy-docker-broker destroy-docker-data destroy-docker-engine-editor destroy-docker-clusters
