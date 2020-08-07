package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rename-secuser-user-1") {
		grailsChange {
			change {
				sql.execute("rename table sec_user to user")
			}
		}
	}
	changeSet(author: "kkn", id: "rename-secuser-user-2") {
		grailsChange {
			change {
				sql.execute("rename table sec_role to role")
			}
		}
	}
}
