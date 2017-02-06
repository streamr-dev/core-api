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

	changeSet(author: "henri", id: "2017-02-06-ethereum-module-category") {
		insert(tableName: "module_category") {
			column(name: "id", valueNumeric: 1001)
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Ethereum")
			column(name: "sort_order", valueNumeric: 0)
			column(name: "parent_id", valueNumeric: 1000) // Integrations
			column(name: "module_package_id", valueNumeric: 1) // Core
		}

		// Move EthereumCall module to Ethereum module category
		update(tableName: "module") {
			column(name: "category_id", valueNumeric: 1001) // Integrations
			where("id = 1020")
		}
	}

	changeSet(author: "henri", id: "2017-02-06-solidity-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1021)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1001) // Ethereum
			column(name: "implementing_class", value: "com.unifina.signalpath.blockchain.SolidityModule")
			column(name: "name", value: "SolidityModule")
			column(name: "js_module", value: "SolidityModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: null)
		}
	}
}