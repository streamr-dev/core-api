package core

databaseChangeLog = {

	// migrate data
	// old system was Canvases with canvas.shared visible to everyone
	// new system is Permission row for each Canvas, with anonymous-bit set (last column)
	changeSet(author: "jtakalai (generated)", id: "1457514548258-1") {
		sql("INSERT INTO permission (id, version, clazz, long_id, operation, string_id, user_id, invite_id, anonymous) " +
				"SELECT NULL, 0, 'com.unifina.domain.signalpath.Canvas', NULL, 'read', id, NULL, NULL, true " +
				"FROM canvas " +
				"WHERE shared=true")

		dropColumn(columnName: "shared", tableName: "canvas")
	}
}
