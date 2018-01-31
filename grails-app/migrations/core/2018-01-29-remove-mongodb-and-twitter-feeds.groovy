package core
databaseChangeLog = {
	changeSet(author: "eric", id: "remove-mongodb-and-twitter-feeds-0") {
		sql("DELETE FROM permission WHERE feed_id = 8 OR feed_id = 9")
	}
	changeSet(author: "eric", id: "remove-mongodb-and-twitter-feeds-1") {
		sql("DELETE FROM feed_file WHERE feed_id = 8 OR feed_id = 9")
	}
	changeSet(author: "eric", id: "remove-mongodb-and-twitter-feeds-2") {
		sql("DELETE FROM stream WHERE feed_id = 8 OR feed_id = 9")
	}
	changeSet(author: "eric", id: "remove-mongodb-and-twitter-feeds-3") {
		sql("DELETE FROM feed WHERE id = 8") // remove mongo
	}
	changeSet(author: "eric", id: "remove-mongodb-and-twitter-feeds-4") {
		sql("DELETE FROM feed WHERE id = 9") // remove twitter
	}
}
