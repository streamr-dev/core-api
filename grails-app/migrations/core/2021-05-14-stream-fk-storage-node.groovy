package core

import com.unifina.utils.IdGenerator

databaseChangeLog = {
	changeSet(author: "kkn", id: "stream-fk-storage-node-1") {
		sql("alter table stream_storage_node drop primary key")
		sql("alter table stream_storage_node drop index stream_storage_node_stream_idx")
		sql("alter table stream_storage_node drop index stream_storage_node_storage_node_address_idx")
		sql("alter table stream_storage_node add id varchar(255) null")
	}
	changeSet(author: "kkn", id: "stream-fk-storage-node-2") {
		createTable(tableName: "stream_stream_storage_node") {
			column(name: "storage_node_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
			column(name: "stream_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "stream-fk-storage-node-3") {
		grailsChange {
			change {
				sql.eachRow("select stream_id, storage_node_address from stream_storage_node") { row ->
					String streamId = row['stream_id']
					String sroageNodeAddress = row['storage_node_address']
					def storageNodeId = IdGenerator.get()
					sql.execute("""update stream_storage_node
                               set id = ?
                               where stream_id = ? and storage_node_address = ?
                               """, [storageNodeId, streamId, sroageNodeAddress])
					sql.execute("""insert into stream_stream_storage_node(storage_node_id, stream_id)
								values(?, (select stream_id from stream_storage_node where id = ?))
								""", [storageNodeId, storageNodeId])
				}
			}
		}
	}
	changeSet(author: "kkn", id: "stream-fk-storage-node-4") {
		sql("set foreign_key_checks = 0")
		sql("create index stream_storage_node_id_idx on stream_stream_storage_node(storage_node_id)")
		sql("create index stream_id_idx on stream_stream_storage_node(stream_id)")
		sql("alter table stream_storage_node add primary key(id)")
		addForeignKeyConstraint(baseColumnNames: "storage_node_id",
			baseTableName: "stream_stream_storage_node",
			constraintName: "fk_stream_storage_node_id",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "stream_storage_node",
			referencesUniqueColumn: "false")
		addForeignKeyConstraint(baseColumnNames: "stream_id",
			baseTableName: "stream_stream_storage_node",
			constraintName: "fk_stream_id",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "stream",
			referencesUniqueColumn: "false")
		sql("set foreign_key_checks = 1")
	}
}
