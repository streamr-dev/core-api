package core

import com.streamr.core.utils.IdGenerator

databaseChangeLog = {
	changeSet(author: "kkn", id: "stream-fk-storage-node-1") {
		sql("alter table stream_storage_node drop primary key")
		sql("alter table stream_storage_node drop index stream_storage_node_stream_idx")
		sql("alter table stream_storage_node drop index stream_storage_node_storage_node_address_idx")
		sql("alter table stream_storage_node add id varchar(255) null")
	}
	changeSet(author: "kkn", id: "stream-fk-storage-node-2") {
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
				}
			}
		}
	}
	changeSet(author: "kkn", id: "stream-fk-storage-node-3") {
		sql("set foreign_key_checks = 0")
		sql("create index storage_node_address_idx on stream_storage_node(storage_node_address)")
		sql("create index stream_id_idx on stream_storage_node(stream_id)")
		sql("alter table stream_storage_node add primary key(id)")
		addForeignKeyConstraint(baseColumnNames: "stream_id",
			baseTableName: "stream_storage_node",
			constraintName: "fk_stream_id",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "stream",
			referencesUniqueColumn: "false")
		sql("set foreign_key_checks = 1")
	}
}
