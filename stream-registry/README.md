
1. Patch Web3j codegen and install it locally:
	```bash
	git clone https://github.com/web3j/web3j.git
	git checkout v4.8.7
	git am 0001-v4.8.7-fix-handle-ParameterizedTypeName.patch
	mvn -B package --file pom.xml
	mvn jar:jar install:install
	```
1. Generate code from StreamRegistryV3 and copy generated file to core-api
   source tree:
	```bash
	git clone git@github.com:streamr-dev/network-contracts.git
	git checkout 32325d6
	make build
	make deploy
	```
