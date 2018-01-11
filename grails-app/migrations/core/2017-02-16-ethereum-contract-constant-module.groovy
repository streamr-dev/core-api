package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2017-02-16-get-contract-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1023)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1001)
			column(name: "implementing_class", value: "com.unifina.signalpath.blockchain.GetEthereumContractAt")
			column(name: "name", value: "GetContractAt")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "alternative_names", value: "ContractAtAddress,GetEthereumContract,GetEthereumContractAt")
			column(name: "json_help", value: '{"helpText":"Ethereum contract that has been deployed in the blockchain"}')
		}
	}
}