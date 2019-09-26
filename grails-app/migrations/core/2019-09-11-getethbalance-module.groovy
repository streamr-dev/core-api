
package core
databaseChangeLog = {

	changeSet(author: "jwolff", id: "2019-09-11-getethbalance-module") {

		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1152)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1001) // Ethereum
			column(name: "implementing_class", value: "com.unifina.signalpath.blockchain.GetEthBalance")
			column(name: "name", value: "GetEthBalance")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"inputs":{"address":"The address whose ETH balance should be checked"},' +
				'"inputNames":["address"],' +
				'"outputs":{"balance":"The ETH balance in Ether"},' +
				'"outputNames":["balance"],' +
				'"helpText":' +
				'"check the ETH balance of an Ethereum address"' +
				'}')
		}
	}
}