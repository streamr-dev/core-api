package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "user-refactor-username-add-email-1") {
		addColumn(tableName: "sec_user") {
			column(name: "email", type: "varchar(255)")
		}
	}
	changeSet(author: "kkn", id: "user-refactor-username-add-email-2") {
		grailsChange {
			change {
				sql.execute("update sec_user set email = username where username like '%@%'")
			}
		}
	}
}
