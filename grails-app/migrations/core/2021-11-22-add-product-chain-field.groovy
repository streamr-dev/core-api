package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "add-product-chain-field-1") {
		sql("alter table product add column chain varchar(50) after terms_of_use_terms_url;")
		sql("update product set chain = 'ETHEREUM';")
		sql("alter table product modify chain varchar(50) not null;")
		sql("alter table product add index product_chain_idx (chain);")
	}
}