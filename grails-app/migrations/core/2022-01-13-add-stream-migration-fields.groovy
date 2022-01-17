package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "add-stream-migration-fields-1") {
		sql("alter table stream add column migrate_to_brubeck bit not null default false after require_encrypted_data;")
		sql("alter table stream add column migrate_sync_turned_on_at datetime after migrate_to_brubeck;")
		sql("alter table stream add column migrate_sync_last_run_at datetime after migrate_sync_turned_on_at;")
	}
}