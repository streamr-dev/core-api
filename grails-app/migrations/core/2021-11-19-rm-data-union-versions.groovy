package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-data-union-versions-1") {
		sql("alter table product drop column data_union_version;")
	}
}