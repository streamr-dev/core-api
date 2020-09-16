package core
databaseChangeLog = {
	changeSet(author: "teogeb", id: "add-stream-storage-node-1") {
		createTable(tableName: "stream_storage_node") {
			column(name: "stream_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
			column(name: "storage_node_address", type: "varchar(255)") {
				constraints(nullable: "false")
			}
			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}
			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "teogeb", id: "add-stream-storage-node-2") {
		addPrimaryKey(columnNames: "stream_id, storage_node_address", constraintName: "stream_storagPK", tableName: "stream_storage_node")
		createIndex(indexName: "stream_storage_node_stream_idx", tableName: "stream_storage_node") {
			column(name: "stream_id")
		}
		createIndex(indexName: "stream_storage_node_storage_node_address_idx", tableName: "stream_storage_node") {
			column(name: "storage_node_address")
		}
	}
}