package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "remove-dead-product-1") {
		sql("delete from permission where product_id = '2d6dbcdb82c94771a2c0b65ee0e219a155cdc406137748c7bd09cc7fc1071142'")
		sql("delete from permission where stream_id = 'RXEgwSxWQZa-zHBfpp1RTg'")
		sql("delete from product_streams where stream_id = 'RXEgwSxWQZa-zHBfpp1RTg'")
		sql("delete from subscription where product_id = '2d6dbcdb82c94771a2c0b65ee0e219a155cdc406137748c7bd09cc7fc1071142'")
		sql("delete from product where id = '2d6dbcdb82c94771a2c0b65ee0e219a155cdc406137748c7bd09cc7fc1071142'")
	}
}
