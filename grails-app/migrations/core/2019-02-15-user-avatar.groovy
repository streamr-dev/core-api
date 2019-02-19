package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "user-avatar-1") {
		addColumn(tableName: "sec_user") {
			column(name: "image_url", type: "varchar(255)")
		}
	}
}
