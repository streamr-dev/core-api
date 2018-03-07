
package core

databaseChangeLog = {
	changeSet(author: "aapeli", id: "2018-03-07-remove-template-field-of-feed") {
		sql("""
			ALTER TABLE feed
			DROP COLUMN stream_page_template;
		""")
	}
}
