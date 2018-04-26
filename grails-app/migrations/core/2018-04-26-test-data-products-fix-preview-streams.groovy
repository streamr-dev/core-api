package core

// CORE-1344: products' preview stream should be one of product's streams (not just bitcoin-twitter)
databaseChangeLog = {
	changeSet(author: "juuso", id: "test-data-products-fix-preview-streams", context: "test") {
		sql("update product, product_streams " +
			"set preview_stream_id = stream_id " +
			"where product_id = product.id;")
	}
}
