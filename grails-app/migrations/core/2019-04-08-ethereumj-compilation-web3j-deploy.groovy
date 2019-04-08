package core
databaseChangeLog = {
	changeSet(author: "jwolff", id: "2019-04-08-ethereumj-compilation-web3j-deploy") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1051)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1001) // Ethereum
			column(name: "implementing_class", value: "com.unifina.signalpath.blockchain.SolidityCompileDeploy")
			column(name: "name", value: "SolidityCompileDeploy")
			column(name: "js_module", value: "SolidityCompileDeploy")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: null)
		}
	}
}
