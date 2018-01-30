package core
databaseChangeLog = {

	changeSet(author: "eric", id: "remove-user-from-domain-objects-1") {
		dropForeignKeyConstraint(baseTableName: "canvas", constraintName: "FKAE7A755860701D32")
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-2") {
		dropForeignKeyConstraint(baseTableName: "dashboard", constraintName: "FKC18AEA9460701D32")
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-3") {
		dropForeignKeyConstraint(baseTableName: "module_package", constraintName: "FK8E99557360701D32")
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-4") {
		dropForeignKeyConstraint(baseTableName: "stream", constraintName: "FKCAD54F8060701D32")
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-5") {
		grailsChange {
			change {
				sql.eachRow("SELECT id, user_id FROM canvas") { row ->
					String id = row["id"]
					String userId = row["user_id"]
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, canvas_id) VALUES (0, ?, ?, 0, ?)', "read", userId, id)
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, canvas_id) VALUES (0, ?, ?, 0, ?)', "write", userId, id)
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, canvas_id) VALUES (0, ?, ?, 0, ?)', "share", userId, id)
				}
			}
		}
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-6") {
		grailsChange {
			change {
				sql.eachRow("SELECT id, user_id FROM dashboard") { row ->
					String id = row["id"]
					String userId = row["user_id"]
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, dashboard_id) VALUES (0, ?, ?, 0, ?)', "read", userId, id)
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, dashboard_id) VALUES (0, ?, ?, 0, ?)', "write", userId, id)
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, dashboard_id) VALUES (0, ?, ?, 0, ?)', "share", userId, id)
				}
			}
		}
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-7") {
		grailsChange {
			change {
				sql.eachRow("SELECT id, user_id FROM module_package") { row ->
					String id = row["id"]
					String userId = row["user_id"]
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, module_package_id) VALUES (0, ?, ?, 0, ?)', "read", userId, id)
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, module_package_id) VALUES (0, ?, ?, 0, ?)', "write", userId, id)
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, module_package_id) VALUES (0, ?, ?, 0, ?)', "share", userId, id)
				}
			}
		}
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-8") {
		grailsChange {
			change {
				sql.eachRow("SELECT id, user_id FROM stream") { row ->
					String id = row["id"]
					String userId = row["user_id"]
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, stream_id) VALUES (0, ?, ?, 0, ?)', "read", userId, id)
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, stream_id) VALUES (0, ?, ?, 0, ?)', "write", userId, id)
					sql.execute('INSERT INTO permission (version, operation, user_id, anonymous, stream_id) VALUES (0, ?, ?, 0, ?)', "share", userId, id)
				}
			}
		}
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-9") {
		dropColumn(columnName: "user_id", tableName: "canvas")
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-10") {
		dropColumn(columnName: "user_id", tableName: "dashboard")
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-11") {
		dropColumn(columnName: "user_id", tableName: "module_package")
	}

	changeSet(author: "eric", id: "remove-user-from-domain-objects-12") {
		dropColumn(columnName: "user_id", tableName: "stream")
	}
}
