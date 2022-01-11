package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "2021-12-15-rm-registration-code-1") {
		sql("drop table registration_code;")
	}
}