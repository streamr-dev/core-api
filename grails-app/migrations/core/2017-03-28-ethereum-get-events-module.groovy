package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2017-03-28-ethereum-get-events-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1032)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1001)
			column(name: "implementing_class", value: "com.unifina.signalpath.blockchain.GetEvents")
			column(name: "name", value: "GetEvents")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "alternative_names", value: "EthereumEvents")
			column(name: "json_help", value: '{"helpText":"Get events sent out by given contract in the given transaction"}')
		}
	}
}
