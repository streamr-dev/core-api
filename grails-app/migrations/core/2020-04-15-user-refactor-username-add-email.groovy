package core

import com.unifina.utils.EmailValidator

databaseChangeLog = {
	changeSet(author: "kkn", id: "user-refactor-username-add-email-1") {
		addColumn(tableName: "sec_user") {
			column(name: "email", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "user-refactor-username-add-email-2") {
		grailsChange {
			change {
				sql.eachRow("select username from sec_user") { row ->
					String username = row["username"]
					if (EmailValidator.validate(username)) {
						sql.execute('update sec_user set email = ? where username = ?', username, username)
					}
				}
			}
		}
	}
	changeSet(author: "kkn", id: "user-refactor-username-add-email-3") {
		dropColumn(columnName: "username", tableName: "sec_user")
	}
	changeSet(author: "kkn", id: "user-refactor-username-add-email-4") {
		createIndex(indexName: "user_email_uniq", tableName: "sec_user", unique: "true") {
			column(name: "email")
		}
	}
	changeSet(author: "kkn", id: "user-refactor-username-add-email-5") {
		addColumn(tableName: "signup_invite") {
			column(name: "email", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "user-refactor-username-add-email-6") {
		grailsChange {
			change {
				sql.eachRow("select username from signup_invite") { row ->
					String username = row["username"]
					sql.execute('update signup_invite set email = ? where username = ?', username, username)
				}
			}
		}	}
	changeSet(author: "kkn", id: "user-refactor-username-add-email-7") {
		dropColumn(columnName: "username", tableName: "signup_invite")
	}
}
