package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2019-03-26-new-ethereum-call-module") {
		//deprecate previous streamr-web3 module
		sql("UPDATE module SET hide = true, name = CONCAT(name, ' (Old)') WHERE id = 1020;")

		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1150)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1001) // Ethereum
			column(name: "implementing_class", value: "com.unifina.signalpath.blockchain.SendEthereumTransaction")
			column(name: "name", value: "EthereumCall")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"params":{"ethAccount":"The account used to make transaction or call", "function":"The contract function to invoke"},' +
				'"paramNames":["ethAccount","function"],' +
				'"inputs":{"contract":"Ethereum contract", "trigger":"Send call (for functions that have no inputs)", "ether":"ETH to send with the function call (for <i>payable</i> functions)"},' +
				'"inputNames":["contract", "trigger", "ether"],' +
				'"outputs":{"errors":"List of error messages"},' +
				'"outputNames":["errors"],' +
				'"helpText":' +
					'"<p>Call Ethereum smart contract.</p>' +
					'<p>First, connect Ethereum contract into <strong>contract</strong>&nbsp;input. You can write your own using SolidityModule, or pick a template such as PayByUse.</p>' +
					'<p>Second, choose the <strong>function</strong> you want to call from the dropdown. There are two kinds of functions calls:</p>' +
					'<ul><li>constant function calls that return results directly, and</li>' +
						'<li>transactions that return values through events that the function call invokes.</li></ul>' +
					'<p>The contract must be deployed before this module can activate.</p>"' +
				'}')
		}
	}
}
