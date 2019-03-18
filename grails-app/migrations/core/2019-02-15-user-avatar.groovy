package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "user-avatar-1") {
		addColumn(tableName: "sec_user") {
			column(name: "image_url_large", type: "varchar(255)")
		}
	}
	changeSet(author: "kkn", id: "user-avatar-2") {
		addColumn(tableName: "sec_user") {
			column(name: "image_url_small", type: "varchar(255)")
		}
	}
}
