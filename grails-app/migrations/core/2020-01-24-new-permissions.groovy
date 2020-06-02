package core
databaseChangeLog = {
	// Stream
	// read -> stream_get, stream_subscribe
	changeSet(author: "kkn", id: "new-permissions-1") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, stream_id, key_id, subscription_id, ends_at, anonymous from permission where stream_id is not null and operation = 'read'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String streamID = row["stream_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'stream_get', ?, ?, ?, ?, ?, ?, ?, ?)", [streamID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'stream_subscribe', ?, ?, ?, ?, ?, ?, ?, ?)", [streamID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}
	// write -> stream_edit, stream_publish, stream_delete
	changeSet(author: "kkn", id: "new-permissions-2") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, stream_id, key_id, subscription_id, ends_at, anonymous from permission where stream_id is not null and operation = 'write'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String streamID = row["stream_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'stream_edit', ?, ?, ?, ?, ?, ?, ?, ?)", [streamID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'stream_publish', ?, ?, ?, ?, ?, ?, ?, ?)", [streamID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'stream_delete', ?, ?, ?, ?, ?, ?, ?, ?)", [streamID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}
	// share -> stream_share
	changeSet(author: "kkn", id: "new-permissions-3") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, stream_id, key_id, subscription_id, ends_at, anonymous from permission where stream_id is not null and operation = 'share'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String streamID = row["stream_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'stream_share', ?, ?, ?, ?, ?, ?, ?, ?)", [streamID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}

	// Canvas
	// read -> canvas_get, canvas_interact
	changeSet(author: "kkn", id: "new-permissions-4") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, canvas_id, key_id, subscription_id, ends_at, anonymous from permission where canvas_id is not null and operation = 'read'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String canvasID = row["canvas_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'canvas_get', ?, ?, ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'canvas_interact', ?, ?, ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}
	// write -> canvas_edit, canvas_startstop, canvas_delete
	changeSet(author: "kkn", id: "new-permissions-5") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, canvas_id, key_id, subscription_id, ends_at, anonymous from permission where canvas_id is not null and operation = 'write'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String canvasID = row["canvas_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'canvas_edit', ?, ?, ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'canvas_startstop', ?, ?, ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'canvas_delete', ?, ?, ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}
	// share -> canvas_share
	changeSet(author: "kkn", id: "new-permissions-6") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, canvas_id, key_id, subscription_id, ends_at, anonymous from permission where canvas_id is not null and operation = 'share'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String canvasID = row["canvas_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'canvas_share', ?, ?, ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}

	// Dashboard
	// read -> dashboard_get, dashboard_interact
	changeSet(author: "kkn", id: "new-permissions-7") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, dashboard_id, key_id, subscription_id, ends_at, anonymous from permission where dashboard_id is not null and operation = 'read'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String dashboardID = row["dashboard_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'dashboard_get', ?, ?, ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'dashboard_interact', ?, ?, ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}
	// write -> dashboard_edit, dashboard_delete
	changeSet(author: "kkn", id: "new-permissions-8") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, dashboard_id, key_id, subscription_id, ends_at, anonymous from permission where dashboard_id is not null and operation = 'write'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String dashboardID = row["dashboard_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'dashboard_edit', ?, ?, ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'dashboard_delete', ?, ?, ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}
	// share -> dashboard_share
	changeSet(author: "kkn", id: "new-permissions-9") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, dashboard_id, key_id, subscription_id, ends_at, anonymous from permission where dashboard_id is not null and operation = 'share'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String dashboardID = row["dashboard_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'dashboard_share', ?, ?, ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}

	// Product
	// read -> product_get
	changeSet(author: "kkn", id: "new-permissions-10") {
		grailsChange {
			change {
				sql.execute("update permission set operation = 'product_get' where product_id is not null and operation = 'read'")
			}
		}
	}
	// write -> product_edit, product_delete
	changeSet(author: "kkn", id: "new-permissions-11") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, product_id, key_id, subscription_id, ends_at, anonymous from permission where product_id is not null and operation = 'write'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String productID = row["product_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, product_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'product_edit', ?, ?, ?, ?, ?, ?, ?, ?)", [productID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
					sql.execute("insert into permission(version, operation, product_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'product_delete', ?, ?, ?, ?, ?, ?, ?, ?)", [productID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}
	// share -> product_share
	changeSet(author: "kkn", id: "new-permissions-12") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, product_id, key_id, subscription_id, ends_at, anonymous from permission where product_id is not null and operation = 'share'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String productID = row["product_id"]
					String keyID = row["key_id"]
					Long subscriptionID = row["subscription_id"]
					Date endsAt = row["ends_at"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, product_id, ends_at, parent_id, anonymous, user_id, invite_id, key_id, subscription_id) values(0, 'product_share', ?, ?, ?, ?, ?, ?, ?, ?)", [productID, endsAt, null, anonymous, userID, inviteID, keyID, subscriptionID])
				}
			}
		}
	}
}
