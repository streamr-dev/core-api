package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "alter-pending-changes-1") {
		sql("alter table product modify column pending_changes text;")
	}
}
