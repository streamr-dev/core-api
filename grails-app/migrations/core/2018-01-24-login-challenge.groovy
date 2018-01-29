package core
databaseChangeLog = {
	changeSet(author: "kkn (generated)", id: "login-challenge-1") {
		createTable(tableName: "challenge") {
			column(name: "id", type: "varchar(255)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "challengeID")
			}
			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}
			column(name: "challenge", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
}
