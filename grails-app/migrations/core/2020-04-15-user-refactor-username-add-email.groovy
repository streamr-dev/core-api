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
	changeSet(author: "kkn", id: "user-refactor-username-add-email-3") {
		grailsChange {
			change {
				sql.execute("alter table signup_invite change username email varchar(255) character set utf8 collate utf8_general_ci not null")
			}
		}
	}
	changeSet(author: "kkn", id: "user-refactor-username-add-email-4") {
		grailsChange {
			change {
				sql.execute("alter table registration_code change username email varchar(255) character set utf8 collate utf8_general_ci not null")
			}
		}
	}
}
