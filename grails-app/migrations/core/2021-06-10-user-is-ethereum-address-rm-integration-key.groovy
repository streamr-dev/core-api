package core

import com.unifina.service.UserAvatarImageService
import groovy.sql.GroovyResultSet

class Key {
	String id
	String idInService
	String name
}

class User {
	Long id
	String email
	String name
	Date lastLogin
	String imageUrlSmall
	String imageUrlLarge
	List<Key> keys = new ArrayList<>()
}

class UserImage {
	Long userId
	String imageUrlSmall
	String imageUrlLarge
}

databaseChangeLog = {
	changeSet(author: "kkn", id: "user-is-ethereum-address-rm-integration-key-1") {
		grailsChange {
			change {
				List<User> users = new ArrayList<>()
				sql.eachRow("""
select user.id, user.name, user.email, user.image_url_large, user.image_url_small, user.last_login
from user
left join integration_key on user.id = integration_key.user_id
group by user.id
having count(integration_key.id) > 1
					""") { GroovyResultSet user ->
					users.add(new User(
						id: user["id"],
						name: user["name"],
						email: user["email"],
						imageUrlLarge: user["image_url_large"],
						imageUrlSmall: user["image_url_small"],
						lastLogin: user["last_login"]))
				}
				println("Found " + users.size() + " users to migrate")

				for (User user : users) {
					Long userId = user.getId()
					println("Collecting integration keys for user with id " + userId)
					// Skip first integration key with:
					//  - offset
					//  - order by date_created
					sql.eachRow("""
select id, id_in_service, name
from integration_key
where service = 'ETHEREUM_ID'
and user_id = ?
order by integration_key.date_created
limit 100000 offset 1
					""", [userId]) { GroovyResultSet key ->
						// TODO: Clone the user to create a newUser
						String id = key["id"]
						String idInService = key["id_in_service"]
						String name = key["name"]
						println("\tAdding key " + id)
						user.getKeys().add(new Key(
							id: id,
							idInService: idInService,
							name: name,
						))
					}
				}

				sql.execute("""
create table user_images_tmp (
	id bigint(20) not null,
	image_url_large varchar(255) default null,
	image_url_small varchar(255) default null,
	primary key (id)
)
				""")
				for (User user : users) {
					println("Converting keys for user with id: " + user.getId())
					for (Key key : user.getKeys()) {
						println("\tConverting integration key to user: " + key.getIdInService())
						sql.execute("""
insert into user(
version, account_expired, account_locked,
enabled, name, username,
date_created, last_login, image_url_large,
image_url_small, email, signup_method)
values(
?, ?, ?,
?, ?, ?,
?, ?, ?,
?, ?, ?)
						""", [
							0, false, false,
							true, String.format("%s: %s", user.getName(), key.getName()), key.getIdInService(),
							new Date(), user.getLastLogin(), null,
							null, user.getEmail(), "MIGRATED",
						])
						// Update the IntegrationKey to point to newUser instead of user
						Long newUserId = sql.firstRow("select last_insert_id() as new_user_id").new_user_id
						sql.execute("update integration_key set user_id = ? where id = ?", [newUserId, key.getId()])
						sql.execute("insert into user_images_tmp(id, image_url_large, image_url_small) values(?, ?, ?)", [newUserId, user.getImageUrlLarge(), user.getImageUrlSmall()])
						// TODO: Clone all of user's permissions to newUser
						sql.eachRow("""
select operation, invite_id, anonymous, stream_id, product_id, subscription_id, ends_at
from permission
where user_id = ?
						""", [newUserId]) { GroovyResultSet permission ->
							String operation = permission["operation"]
							Long inviteId = permission["invite_id"]
							String streamId = permission["stream_id"]
							String productId = permission["product_id"]
							Long subscriptionId = permission["subscription_id"]
							Date endsAt = permission["ends_at"]
							sql.execute("""
insert into permission(
version, operation, user_id,
invite_id, anonymous, stream_id,
product_id, subscription_id, ends_at
) values(
?, ?, ?,
?, ?, ?,
?, ?, ?)
							""", [
								0, operation, newUserId,
								inviteId, false, streamId,
								productId, subscriptionId, endsAt
							])
						}

						sql.eachRow("""
select operation, invite_id, anonymous, stream_id, product_id, subscription_id, ends_at
from permission
where user_id = ? and anonymous = true
						""", [newUserId]) { GroovyResultSet permission ->
							String operation = permission["operation"]
							Long inviteId = permission["invite_id"]
							String streamId = permission["stream_id"]
							String productId = permission["product_id"]
							Long subscriptionId = permission["subscription_id"]
							Date endsAt = permission["ends_at"]
							sql.execute("""
insert into permission(
version, operation, user_id,
invite_id, anonymous, stream_id,
product_id, subscription_id, ends_at
) values(
?, ?, ?,
?, ?, ?,
?, ?, ?)
							""", [
								0, operation, newUserId,
								inviteId, false, streamId,
								productId, subscriptionId, endsAt
							])
						}
					}
				}
			}
		}
	}

	changeSet(author: "kkn", id: "user-is-ethereum-address-rm-integration-key-2") {
		grailsChange {
			change {
				sql.eachRow("""
select user.id, user.username, integration_key.id, integration_key.id_in_service
from user
left join integration_key on user.id = integration_key.user_id
where id_in_service is not null
			""") { GroovyResultSet rs ->
					String userId = rs["user.id"]
					String address = rs["integration_key.id_in_service"]
					sql.execute("""
update user set username = ?
where id = ?
					""", [address, userId])

				}
			}
		}
	}

	changeSet(author: "kkn", id: "user-is-ethereum-address-rm-integration-key-3") {
		grailsChange {
			change {
				UserAvatarImageService userAvatarImageService = ctx.getBean("userAvatarImageService")
				List<UserImage> images = new ArrayList<>()
				sql.eachRow("""
select id, image_url_large, image_url_small
from user_images_tmp
		""") { GroovyResultSet rs ->
					String imageUrlLarge = rs["image_url_large"]
					String imageUrlSmall = rs["image_url_small"]
					String newImageUrlLarge = null
					String newImageUrlSmall = null
					if (imageUrlLarge != null) {
						newImageUrlLarge = userAvatarImageService.copyImage(imageUrlLarge)
					}
					if (imageUrlSmall != null) {
						newImageUrlSmall = userAvatarImageService.copyImage(imageUrlSmall)
					}
					if (newImageUrlLarge != null && newImageUrlSmall != null) {
						Long id = rs["id"]
						if (id != null) {
							images.add(new UserImage(
								userId: id,
								imageUrlLarge: newImageUrlLarge,
								imageUrlSmall: newImageUrlSmall,
							))
						}
					}
				}
				for (UserImage image : images) {
					sql.executeUpdate("""
update user
set image_url_large = ?, image_url_small = ?
where id = ?
				""", [image.getImageUrlLarge(), image.getImageUrlSmall(), image.getUserId()])

				}
			}

		}
	}
}

/*
permission
+-----------------+--------------+------+-----+---------+----------------+
| Field           | Type         | Null | Key | Default | Extra          |
+-----------------+--------------+------+-----+---------+----------------+
| id              | bigint(20)   | NO   | PRI | NULL    | auto_increment |
| version         | bigint(20)   | NO   |     | NULL    |                |
| operation       | varchar(255) | NO   |     | NULL    |                |
| user_id         | bigint(20)   | YES  | MUL | NULL    |                |
| invite_id       | bigint(20)   | YES  | MUL | NULL    |                |
| anonymous       | bit(1)       | NO   | MUL | NULL    |                |
| stream_id       | varchar(255) | YES  | MUL | NULL    |                |
| product_id      | varchar(255) | YES  | MUL | NULL    |                |
| subscription_id | bigint(20)   | YES  | MUL | NULL    |                |
| ends_at         | datetime     | YES  |     | NULL    |                |
| parent_id       | bigint(20)   | YES  | MUL | NULL    |                |
+-----------------+--------------+------+-----+---------+----------------+
11 rows in set (0,08 sec)

user
+-----------------+--------------+------+-----+---------+----------------+
| Field           | Type         | Null | Key | Default | Extra          |
+-----------------+--------------+------+-----+---------+----------------+
| id              | bigint(20)   | NO   | PRI | NULL    | auto_increment |
| version         | bigint(20)   | NO   |     | NULL    |                |
| account_expired | bit(1)       | NO   |     | NULL    |                |
| account_locked  | bit(1)       | NO   |     | NULL    |                |
| enabled         | bit(1)       | NO   |     | NULL    |                |
| name            | varchar(255) | NO   |     | NULL    |                |
| username        | varchar(255) | NO   |     | NULL    |                |
| date_created    | datetime     | YES  |     | NULL    |                |
| last_login      | datetime     | YES  |     | NULL    |                |
| image_url_large | varchar(255) | YES  |     | NULL    |                |
| image_url_small | varchar(255) | YES  |     | NULL    |                |
| email           | varchar(255) | YES  |     | NULL    |                |
| signup_method   | varchar(255) | NO   |     | NULL    |                |
+-----------------+--------------+------+-----+---------+----------------+

integration_key
+---------------+--------------+------+-----+---------+-------+
| Field         | Type         | Null | Key | Default | Extra |
+---------------+--------------+------+-----+---------+-------+
| id            | varchar(255) | NO   | PRI | NULL    |       |
| version       | bigint(20)   | NO   |     | NULL    |       |
| name          | varchar(255) | NO   |     | NULL    |       |
| date_created  | datetime     | NO   |     | NULL    |       |
| last_updated  | datetime     | NO   |     | NULL    |       |
| json          | longtext     | NO   |     | NULL    |       |
| user_id       | bigint(20)   | NO   | MUL | NULL    |       |
| service       | varchar(255) | NO   |     | NULL    |       |
| id_in_service | varchar(255) | NO   | UNI | NULL    |       |
+---------------+--------------+------+-----+---------+-------+
*/
