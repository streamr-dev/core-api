package core
databaseChangeLog = {
	changeSet(author: "teogeb", id: "remove-anonymous-keys-delete-and-edit-permissions-1") {
		sql("DELETE FROM permission WHERE key_id is not null and (operation='stream_delete' or operation='stream_edit')");
	}
}