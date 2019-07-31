package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "enable-stream-module-1") {
		grailsChange {
			change {
				sql.execute('UPDATE module SET category_id = 53, hide = NULL WHERE id = 147')
			}
		}
	}
}
