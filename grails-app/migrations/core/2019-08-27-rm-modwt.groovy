package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-modwt-1") {
		grailsChange {
			change {
				sql.execute('DELETE FROM module WHERE id = 70')
			}
		}
	}
}
