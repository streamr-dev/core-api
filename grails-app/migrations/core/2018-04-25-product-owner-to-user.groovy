package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "product-owner-to-user-1") {
		addColumn(tableName: "product") {
			column(name: "owner_id", type: "bigint") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "kkn", id: "product-owner-to-user-2") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "product", constraintName: "fk_product_user", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sec_user", referencesUniqueColumn: "false")
	}
	changeSet(author: "kkn", id: "product-owner-to-user-3") {
		grailsChange {
			change {
				sql.eachRow('SELECT id, owner FROM product') { row ->
					String productId = row['id']
					String owner = row['owner']
					sql.query("SELECT id FROM sec_user WHERE username = ?", [owner]) { rs ->
						Long userId = 1 // fallback to default user
						if (rs.next()) {
							userId = rs.getLong(1)
						}
						sql.execute('UPDATE product SET owner_id = ? WHERE id = ?', [userId, productId])
					}
				}
			}
		}
	}
	changeSet(author: "kkn", id: "product-owner-to-user-4") {
		dropColumn(columnName: "owner", tableName: "product")
	}
	changeSet(author: "kkn", id: "product-owner-to-user-5") {
		addNotNullConstraint(columnDataType: "bigint", columnName: "owner_id", tableName: "product")
	}
	changeSet(author: "kkn", id: "product-owner-to-user-6") {
		createIndex(indexName: "product_owner_id_idx", tableName: "product") {
			column(name: "owner_id")
		}
	}
}
