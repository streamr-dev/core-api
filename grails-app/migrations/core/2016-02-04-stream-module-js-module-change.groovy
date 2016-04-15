package core
databaseChangeLog = {

	changeSet(author: "aapeli", id: "1452621480275-10") {
		sql("""
			UPDATE module
			SET js_module="StreamModule"
			WHERE id=147;
		""")
	}

}
