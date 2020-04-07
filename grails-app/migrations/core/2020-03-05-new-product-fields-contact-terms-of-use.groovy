package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-1") {
		addColumn(tableName: "product") {
			column(name: "contact_email", type: "varchar(255)")
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-2") {
		addColumn(tableName: "product") {
			column(name: "contact_social1", type: "varchar(2048)")
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-3") {
		addColumn(tableName: "product") {
			column(name: "contact_social2", type: "varchar(2048)")
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-4") {
		addColumn(tableName: "product") {
			column(name: "contact_social3", type: "varchar(2048)")
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-5") {
		addColumn(tableName: "product") {
			column(name: "contact_social4", type: "varchar(2048)")
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-6") {
		addColumn(tableName: "product") {
			column(name: "contact_url", type: "varchar(2048)")
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-7") {
		addColumn(tableName: "product") {
			column(name: "terms_of_use_commercial_use", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-8") {
		addColumn(tableName: "product") {
			column(name: "terms_of_use_redistribution", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-9") {
		addColumn(tableName: "product") {
			column(name: "terms_of_use_reselling", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-10") {
		addColumn(tableName: "product") {
			column(name: "terms_of_use_storage", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-11") {
		addColumn(tableName: "product") {
			column(name: "terms_of_use_terms_name", type: "varchar(100)")
		}
	}
	changeSet(author: "kkn", id: "new-product-fields-contact-terms-of-use-12") {
		addColumn(tableName: "product") {
			column(name: "terms_of_use_terms_url", type: "varchar(2048)")
		}
	}
}
