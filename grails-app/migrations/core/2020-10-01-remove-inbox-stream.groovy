package core
databaseChangeLog = {
	changeSet(author: "teogeb", id: "remove-inbox-stream-1") {
		sql("DELETE FROM permission WHERE stream_id IN (SELECT id FROM stream WHERE inbox = b'1')");
	}
	changeSet(author: "teogeb", id: "remove-inbox-stream-2") {
		sql("DELETE FROM stream WHERE inbox = b'1'");
	}
	changeSet(author: "teogeb", id: "remove-inbox-stream-3") {
		dropColumn(columnName: "inbox", tableName: "stream")
	}
}
