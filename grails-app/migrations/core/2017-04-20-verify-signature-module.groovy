package core
databaseChangeLog = {
	changeSet(author: "eric", id: "verify-signature-module-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 574)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1001)
			column(name: "implementing_class", value: "com.unifina.signalpath.blockchain.VerifySignature")
			column(name: "name", value: "VerifySignature")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "alternative_names", value: "GetSignature")
			column(name: "json_help", value: '{"helpText":"Given message and signature get Ethereum address of signee."}')
		}
	}
}