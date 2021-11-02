package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "delete-integrationkey-1") {
		sql("drop table integration_key")
	}
}