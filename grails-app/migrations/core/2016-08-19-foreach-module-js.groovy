package core
databaseChangeLog = {

	changeSet(author: "henri", id: "2016-08-19-foreach-module-js") {
		sql("UPDATE `module` SET `js_module` = 'ForEachModule' WHERE `id` = '223'")
	}
}
