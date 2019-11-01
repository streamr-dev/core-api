package core
databaseChangeLog = {
	changeSet(author: "mthambipillai", id: "rename-key-provider-1") {
		sql("""
			UPDATE feed SET
				key_provider_class = 'com.unifina.feed.StreamMessageKeyProvider'
				WHERE `id` = '7';
		""")
	}
}
