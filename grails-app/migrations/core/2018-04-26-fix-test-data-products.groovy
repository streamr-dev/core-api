package core

databaseChangeLog = {
	// CORE-1344: products' preview stream should be one of product's streams (not just bitcoin-twitter)
	changeSet(author: "juuso", id: "test-data-products-fix-preview-streams", context: "test") {
		sql("update product, product_streams " +
			"set preview_stream_id = stream_id " +
			"where product_id = product.id;")
	}

	// CORE-1344: USD prices also with 18 decimals
	changeSet(author: "juuso", id: "test-data-products-fix-usd-prices", context: "test") {
		sql('update product ' +
			'set price_per_second = price_per_second * 1000000000 ' +
			'where price_currency = "USD"')
	}
}
