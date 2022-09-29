package core

databaseChangeLog = {
	changeSet(author: "henri", id: "store-product-pricePerSecond-as-string-1") {
		sql("ALTER TABLE product MODIFY COLUMN price_per_second VARCHAR(255) NOT NULL;")
		sql("UPDATE product set price_per_second=CONCAT(price_per_second, \"000000000\") where price_per_second != \"0\";")
	}
}