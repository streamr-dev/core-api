package core
databaseChangeLog = {

	changeSet(author: "henripihkala (generated)", id: "1474977762795-1") {
		addColumn(tableName: "stream") {
			column(name: "partitions", type: "integer", defaultValue: 1) {
				constraints(nullable: "false")
			}
		}
	}
}
