package core
databaseChangeLog = {
	/**
	 * Annoyingly, some of the constraint names are different in prod database vs. dev/test.
	 * Below, we try both possible constraint names.
	 */
	changeSet(author: "hpihkala", id: "drop-feed-to-module-id-constraint") {
		grailsChange {
			change {
				try {
					// dev & test
					sql.execute("ALTER TABLE `feed` DROP FOREIGN KEY `FK2FE59EB6140F06`")
				} catch (Exception e) {
					// prod
					sql.execute("ALTER TABLE `feed` DROP FOREIGN KEY `FK2FE59E26BD3526`")
				}
			}
		}
	}
	changeSet(author: "hpihkala", id: "drop-stream-to-feed-id-constraint") {
		grailsChange {
			change {
				try {
					// dev & test
					sql.execute("ALTER TABLE `stream` DROP FOREIGN KEY `FKCAD54F8072507A49`")
				} catch (Exception e) {
					// prod
					sql.execute("ALTER TABLE `stream` DROP FOREIGN KEY `FK4DA325A8F511C069`")
				}
			}
		}
	}
	changeSet(author: "hpihkala", id: "drop-feed-2") {
		dropForeignKeyConstraint(baseTableName: "permission", constraintName: "FKE125C5CF72507A49")
	}
	changeSet(author: "hpihkala", id: "drop-feed-4") {
		dropColumn(columnName: "feed_id", tableName: "permission")
	}
	changeSet(author: "hpihkala", id: "drop-feed-5") {
		dropColumn(columnName: "feed_id", tableName: "stream")
	}
	changeSet(author: "hpihkala", id: "drop-feed-6") {
		dropTable(tableName: "feed")
	}
}
