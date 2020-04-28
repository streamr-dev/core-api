package core

import com.unifina.utils.EmailValidator

databaseChangeLog = {
	changeSet(author: "kkn", id: "user-refactor-username-add-email-1") {
		addColumn(tableName: "sec_user") {
			column(name: "email", type: "varchar(255)")
		}
	}
	changeSet(author: "kkn", id: "user-refactor-username-add-email-2") {
		grailsChange {
			change {
				sql.eachRow("select username from sec_user") { row ->
					String username = row["username"]
					if (EmailValidator.validate(username)) {
						sql.execute("update sec_user set email = ? where username = ?", username, username)
					}
				}
			}
		}
	}
}
