package core

databaseChangeLog = {

	changeSet(author: "jtakalai (generated)", id: "1455013969117-1") {
		addColumn(tableName: "permission") {
			column(name: "invite_id", type: "bigint")
		}
	}

	changeSet(author: "jtakalai (generated)", id: "1455013969117-3") {
		dropNotNullConstraint(columnDataType: "bigint", columnName: "user_id", tableName: "permission")
	}

	changeSet(author: "jtakalai (generated)", id: "1455013969117-5") {
		createIndex(indexName: "FKE125C5CF8377B94B", tableName: "permission") {
			column(name: "invite_id")
		}
	}

	changeSet(author: "jtakalai (generated)", id: "1455013969117-4") {
		addForeignKeyConstraint(baseColumnNames: "invite_id", baseTableName: "permission", constraintName: "FKE125C5CF8377B94B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "signup_invite", referencesUniqueColumn: "false")
	}
}
