package core
databaseChangeLog = {

	changeSet(author: "henri", id: "2016-08-19-foreach-module-js") {
		sql("UPDATE `module` SET `js_module` = 'ForEachModule' WHERE `id` = '223'")
	}

	changeSet(author: "henri", id: "2016-09-01-canvas-module-js") {
		sql("UPDATE `module` SET `js_module` = 'CanvasModule' WHERE `id` = '81'")
	}

}
