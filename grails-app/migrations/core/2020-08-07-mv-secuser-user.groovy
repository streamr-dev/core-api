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
	changeSet(author: "kkn", id: "rename-secuser-user-3") {
		grailsChange {
			change {
				sql.execute("rename table sec_user_sec_role to user_role")
				sql.execute("alter table user_role change sec_role_id role_id bigint(20) not null")
				sql.execute("alter table user_role change sec_user_id user_id bigint(20) not null")
			}
		}
	}
}
