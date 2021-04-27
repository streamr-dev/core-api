package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "remove-canvas-dashboard-module-1") {
		dropTable(tableName: "module")
	}
	changeSet(author: "kkn", id: "remove-canvas-dashboard-module-2") {
		dropForeignKeyConstraint(baseTableName: "canvas", constraintName: "FKAE7A7558BA6E1FE8")
		dropForeignKeyConstraint(baseTableName: "permission", constraintName: "FKE125C5CF3D649786")
		dropForeignKeyConstraint(baseTableName: "stream", constraintName: "FKCAD54F8052E2E25F")
		dropTable(tableName: "canvas")
	}
	changeSet(author: "kkn", id: "remove-canvas-dashboard-module-3") {
		dropForeignKeyConstraint(baseTableName: "permission", constraintName: "FKE125C5CF70E281EB")
		dropTable(tableName: "dashboard")
	}
	changeSet(author: "kkn", id: "remove-canvas-dashboard-module-4") {
		dropUniqueConstraint(tableName: "permission", constraintName: "FKE125C5CF3D649786")
		dropUniqueConstraint(tableName: "permission", constraintName: "FKE125C5CF70E281EB")
		dropColumn(tableName: "permission", columnName: "canvas_id")
		dropColumn(tableName: "permission", columnName: "dashboard_id")
		sql("delete from permission where operation like 'canvas_%'")
		sql("delete from permission where operation like 'dashboard_%'")
	}
	changeSet(author: "kkn", id: "remove-canvas-dashboard-module-5") {
		dropUniqueConstraint(tableName: "stream", constraintName: "FKCAD54F8052E2E25F")
		dropUniqueConstraint(tableName: "stream", constraintName: "ui_channel_path_idx")
		sql("delete from stream where ui_channel = 1")
		dropColumn(tableName: "stream", columnName: "ui_channel_canvas_id")
		dropColumn(tableName: "stream", columnName: "ui_channel_path")
		dropColumn(tableName: "stream", columnName: "ui_channel")
	}
	changeSet(author: "kkn", id: "remove-canvas-dashboard-module-6") {
		sql("delete from integration_key where service = 'ETHEREUM'")
	}
}
