
package core

databaseChangeLog = {
	changeSet(author: "aapeli", id: "2018-03-07-remove-template-field-of-feed") {
		dropColumn(columnName: "stream_page_template", tableName: "feed")
	}
}
