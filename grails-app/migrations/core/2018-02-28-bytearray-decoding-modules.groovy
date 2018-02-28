package core
databaseChangeLog = {
	changeSet(author: "jarno", id: "decode-string-to-byte-array") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 6000)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 27)
			column(name: "implementing_class", value: "com.unifina.signalpath.text.DecodeStringToByteArray")
			column(name: "name", value: "DecodeStringToByteArray")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '')
		}
	}
	changeSet(author: "jarno", id: "decode-byte-array-to-string") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 6001)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 27)
			column(name: "implementing_class", value: "com.unifina.signalpath.text.DecodeByteArrayToString")
			column(name: "name", value: "DecodeByteArrayToString")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '')
		}
	}
}