package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "stream-fulltext-search-1") {
		sql("ALTER TABLE `stream` ADD FULLTEXT INDEX `name_description_fulltext_idx` (`name`, `description`);")
	}
}