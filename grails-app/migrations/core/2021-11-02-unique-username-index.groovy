package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "unique-username-index-1") {
		sql("alter table user add unique index username_idx (username);")
	}
	changeSet(author: "kkn", id: "unique-username-index-2") {
		// alter table permission drop index stream_id_operation_anonymous_idx;
		sql("alter table `permission` add index `stream_id_operation_anonymous_idx` (`stream_id`, `operation`, `anonymous`);")
	}
}