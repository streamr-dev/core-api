package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2017-01-20-ethereum-call-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1020)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3) // Utils
			column(name: "implementing_class", value: "com.unifina.signalpath.blockchain.EthereumCall")
			column(name: "name", value: "EthereumCall")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: null)
		}
	}
}