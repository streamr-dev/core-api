package core
databaseChangeLog = {
	// Stream
	// read -> stream_get, stream_subscribe
	changeSet(author: "kkn", id: "new-permissions-1") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, stream_id, ends_at, parent_id, anonymous from permission where stream_id is not null and operation = 'read'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String streamID = row["stream_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'stream_get', ?, ?, ?, ?, ?, ?)", [streamID, endsAt, parentID, anonymous, userID, inviteID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'stream_subscribe', ?, ?, ?, ?, ?, ?)", [streamID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}
	// write -> stream_edit, stream_publish, stream_delete
	changeSet(author: "kkn", id: "new-permissions-2") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, stream_id, ends_at, parent_id, anonymous from permission where stream_id is not null and operation = 'write'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String streamID = row["stream_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'stream_edit', ?, ?, ?, ?, ?, ?)", [streamID, endsAt, parentID, anonymous, userID, inviteID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'stream_publish', ?, ?, ?, ?, ?, ?)", [streamID, endsAt, parentID, anonymous, userID, inviteID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'stream_delete', ?, ?, ?, ?, ?, ?)", [streamID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}
	// share -> stream_share
	changeSet(author: "kkn", id: "new-permissions-3") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, stream_id, ends_at, parent_id, anonymous from permission where stream_id is not null and operation = 'share'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String streamID = row["stream_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, stream_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'stream_share', ?, ?, ?, ?, ?, ?)", [streamID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}

	// Canvas
	// read -> canvas_get, canvas_interact
	changeSet(author: "kkn", id: "new-permissions-4") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, canvas_id, ends_at, parent_id, anonymous from permission where canvas_id is not null and operation = 'read'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String canvasID = row["canvas_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'canvas_get', ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, parentID, anonymous, userID, inviteID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'canvas_interact', ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}
	// write -> canvas_edit, canvas_startstop, canvas_delete
	changeSet(author: "kkn", id: "new-permissions-5") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, canvas_id, ends_at, parent_id, anonymous from permission where canvas_id is not null and operation = 'write'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String canvasID = row["canvas_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'canvas_edit', ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, parentID, anonymous, userID, inviteID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'canvas_startstop', ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, parentID, anonymous, userID, inviteID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'canvas_delete', ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}
	// share -> canvas_share
	changeSet(author: "kkn", id: "new-permissions-6") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, canvas_id, ends_at, parent_id, anonymous from permission where canvas_id is not null and operation = 'share'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String canvasID = row["canvas_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, canvas_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'canvas_share', ?, ?, ?, ?, ?, ?)", [canvasID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}

	// Dashboard
	// read -> dashboard_get, dashboard_interact
	changeSet(author: "kkn", id: "new-permissions-7") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, dashboard_id, ends_at, parent_id, anonymous from permission where dashboard_id is not null and operation = 'read'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String dashboardID = row["dashboard_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'dashboard_get', ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, parentID, anonymous, userID, inviteID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'dashboard_interact', ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}
	// write -> dashboard_edit, dashboard_delete
	changeSet(author: "kkn", id: "new-permissions-8") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, dashboard_id, ends_at, parent_id, anonymous from permission where dashboard_id is not null and operation = 'write'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String dashboardID = row["dashboard_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'dashboard_edit', ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, parentID, anonymous, userID, inviteID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'dashboard_delete', ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}
	// share -> dashboard_share
	changeSet(author: "kkn", id: "new-permissions-9") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, dashboard_id, ends_at, parent_id, anonymous from permission where dashboard_id is not null and operation = 'share'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String dashboardID = row["dashboard_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, dashboard_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'dashboard_share', ?, ?, ?, ?, ?, ?)", [dashboardID, endsAt, parentID, anonymous, userID, inviteID])
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
				sql.eachRow("select id, user_id, invite_id, product_id, ends_at, parent_id, anonymous from permission where product_id is not null and operation = 'write'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String productID = row["product_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, product_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'product_edit', ?, ?, ?, ?, ?, ?)", [productID, endsAt, parentID, anonymous, userID, inviteID])
					sql.execute("insert into permission(version, operation, product_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'product_delete', ?, ?, ?, ?, ?, ?)", [productID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}
	// share -> product_share
	changeSet(author: "kkn", id: "new-permissions-12") {
		grailsChange {
			change {
				sql.eachRow("select id, user_id, invite_id, product_id, ends_at, parent_id, anonymous from permission where product_id is not null and operation = 'share'") { row ->
					Long permissionID = row["id"]
					Long userID = row["user_id"]
					Long inviteID = row["invite_id"]
					String productID = row["product_id"]
					Date endsAt = row["ends_at"]
					Long parentID = row["parent_id"]
					Boolean anonymous = row["anonymous"]
					sql.execute("delete from permission where id = ?", [permissionID])
					sql.execute("insert into permission(version, operation, product_id, ends_at, parent_id, anonymous, user_id, invite_id) values(0, 'product_share', ?, ?, ?, ?, ?, ?)", [productID, endsAt, parentID, anonymous, userID, inviteID])
				}
			}
		}
	}
}
