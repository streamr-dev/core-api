package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "add-product-written-to-chain-1") {
		sql("alter table product add column written_to_chain bit not null default false after chain;")
		sql("update product set written_to_chain = true where product.state = 'DEPLOYED';")
	}
}