package core

databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2016030700000-1") {
		insert(tableName: "permission") {
			//column(name: "id", valueNumeric: autoincremented?)
			column(name: "version", valueNumeric: 0)
			column(name: "clazz", value: "com.unifina.domain.signalpath.ModulePackage")
			column(name: "long_id", valueNumeric: 1)    // "core" modulepackage
			column(name: "operation", value: "read")
			column(name: "anonymous", valueBoolean: true)
		}
		insert(tableName: "permission") {
			//column(name: "id", valueNumeric: autoincremented?)
			column(name: "version", valueNumeric: 0)
			column(name: "clazz", value: "com.unifina.domain.data.Feed")
			column(name: "long_id", valueNumeric: 7)    // "API" feed
			column(name: "operation", value: "read")
			column(name: "anonymous", valueBoolean: true)
		}
	}
}
