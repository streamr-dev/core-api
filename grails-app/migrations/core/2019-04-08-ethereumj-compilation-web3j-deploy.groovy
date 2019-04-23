package core
databaseChangeLog = {
	changeSet(author: "jwolff", id: "2019-04-08-ethereumj-compilation-web3j-deploy") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1151)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1001) // Ethereum
			column(name: "implementing_class", value: "com.unifina.signalpath.blockchain.SolidityCompileDeploy")
			column(name: "name", value: "SolidityCompileDeploy")
			column(name: "js_module", value: "SolidityModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"params":{"ethAccount":"The account used to deploy contract", "initial ETH":"initial ETH amount to be deployed with contract"},' +
				'"paramNames":["ethAccount", "initial ETH"],' +
				'"inputs":{},' +
				'"inputNames":[],' +
				'"outputs":{"contract":"Ethereum contract"},' +
				'"outputNames":["contract"],' +
				'"helpText":' +
				'"<p>Compile and deploy Ethereum smart contract. Edit the code in text window, close window and then contract will be compiled. Enter constructor args and initial ETH (if applicable) and press deploy. Deployed address will be displayed in bottom text field. ' +
				' You can connect the contract output to SendEthereumTransaction module.</p>"' +
				'}')
		}
	}
}
