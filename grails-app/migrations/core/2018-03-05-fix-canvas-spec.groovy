
package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "fix-canvas-spec-1", context: "test") {
		sql("""
			UPDATE canvas
			SET request_url = 'http://localhost:8081/unifina-core/api/live/request'
			WHERE id = 'run-canvas-spec';
		""")
	}

	changeSet(author: "eric", id: "fix-canvas-spec-2", context: "test") {
		sql("""
			UPDATE canvas
			SET request_url = 'http://127.0.0.1:8081/unifina-core/api/live/request'
			WHERE id = 'run-canvas-spec';
		""")
	}
}
