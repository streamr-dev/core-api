package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "permission-index-1") {
		sql("ALTER TABLE `permission` ADD INDEX `operation_anonymous_idx` (`operation`, `anonymous`);")
	}
	changeSet(author: "kkn", id: "permission-index-2") {
		sql("ALTER TABLE `permission` DROP INDEX `anonymous_idx`;")
	}
}