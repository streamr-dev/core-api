package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "unique-username-index-1") {
		sql("alter table user add unique index (username);")
	}
}