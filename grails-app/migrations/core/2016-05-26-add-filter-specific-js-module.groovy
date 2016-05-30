package core
databaseChangeLog = {

	changeSet(author: "eric", id: "add-filter-specific-js-module") {
		sql("UPDATE module SET js_module = 'FilterModule' WHERE name = 'Filter'")
	}
}
