package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-stream-1") {
		sql("alter table product_streams drop foreign key fk_productstreams_stream;")
		sql("alter table product_streams drop key fk_productstreams_stream;")
		sql("alter table product drop foreign key fk_product_previewstream;")
		sql("alter table product drop key fk_product_previewstream;")
	}

	changeSet(author: "kkn", id: "rm-stream-2") {
		sql("alter table permission drop foreign key FKE125C5CF86527F49;")
		sql("alter table permission drop index stream_id_operation_anonymous_idx;")
		sql("alter table permission drop column stream_id;")
	}

	changeSet(author: "kkn", id: "rm-stream-3") {
		sql("alter table stream_storage_node drop key storage_node_address_idx;")
		sql("alter table stream_storage_node drop foreign key fk_stream_id;")
		sql("alter table stream_storage_node drop key stream_id_idx;")
		sql("alter table stream_storage_node drop primary key;")
		sql("drop table stream_storage_node;")
	}

	changeSet(author: "kkn", id: "rm-stream-4") {
		sql("alter table stream drop key name_idx;")
		sql("alter table stream drop key uuid_idx;")
		sql("alter table stream drop key example_type_idx;")
		sql("alter table stream drop index name_description_fulltext_idx;")
		sql("alter table stream drop primary key;")
		sql("drop table stream;")
	}

	changeSet(author: "kkn", id: "rm-stream-5") {
		sql("delete from permission where operation = 'stream_get';")
		sql("delete from permission where operation = 'stream_edit';")
		sql("delete from permission where operation = 'stream_delete';")
		sql("delete from permission where operation = 'stream_publish';")
		sql("delete from permission where operation = 'stream_subscribe';")
		sql("delete from permission where operation = 'stream_share';")
	}

	changeSet(author: "kkn", id: "rm-stream-6") {
		sql("alter table permission drop foreign key FKE125C5CF8377B94B;")
		sql("alter table permission drop key FKE125C5CF8377B94B;")
		sql("alter table permission drop column invite_id;")
		sql("drop table signup_invite;")
	}
}