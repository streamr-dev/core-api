package core
databaseChangeLog = {
	changeSet(author: "juuso", id: "add-altname-to-expression-module") {
		update(tableName: "module") {
			column(name: "alternative_names", value: "Formula, Evaluate")
			where("id = 567")
		}
	}
}
