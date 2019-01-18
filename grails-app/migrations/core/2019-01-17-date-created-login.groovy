package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "date-created-login-1") {
		addColumn(tableName: "sec_user") {
			column(name: "date_created", type: "datetime") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "kkn", id: "date-created-login-2") {
		grailsChange {
			change {
				sql.execute('UPDATE sec_user SET date_created = ?', [new Date(0)])
			}
		}
	}

	changeSet(author: "kkn", id: "date-created-login-3") {
		addColumn(tableName: "sec_user") {
			column(name: "last_login", type: "datetime") {
				constraints(nullable: "true")
			}
		}
	}
}
