package core

import com.unifina.domain.security.Permission

databaseChangeLog = {
	// See grails-app/migrations/core/2018-05-02-test-data-products-subscriptions.groovy
	changeSet(author: "kkn", id: "test-data-products-permissions-1", context: "test") {
		grailsChange {
			change {
				sql.eachRow("select id from product") { row ->
					String id = row['id']
					sql.execute("delete from permission where product_id = ?", [id])
					Permission.Operation.productOperations()*.id.each { operation ->
						sql.execute("insert into permission(version, product_id, operation, user_id, anonymous) values(0, ?, ?, 1, 0)", [id, operation])
					}
					sql.execute("insert into permission(version, product_id, operation, anonymous) values(0, ?, 'product_get', 1)", [id])
				}
			}
		}
	}
}
