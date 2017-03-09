package core
databaseChangeLog = {

	changeSet(author: "henripihkala (generated)", id: "1489080031788-1") {
		addColumn(tableName: "stream") {
			column(name: "ui_channel", type: "bit")
		}
	}
}
