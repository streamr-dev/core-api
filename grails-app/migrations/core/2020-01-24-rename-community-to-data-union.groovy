package core
databaseChangeLog = {
	changeSet(author: "hpihkala", id: "rename-community-tables") {
		grailsChange {
			change {
				sql.execute('RENAME TABLE `community_join_request` TO `data_union_join_request`')
				sql.execute('RENAME TABLE `community_secret` TO `data_union_secret`')
				sql.execute('ALTER TABLE `data_union_join_request` CHANGE `community_address` `contract_address` VARCHAR(255)  CHARACTER SET utf8  COLLATE utf8_general_ci  NOT NULL  DEFAULT \'\'')
			}
		}
	}

	changeSet(author: "hpihkala", id: "rename-community-tables-2") {
		grailsChange {
			change {
				sql.execute('ALTER TABLE `data_union_secret` CHANGE `community_address` `contract_address` VARCHAR(255)  CHARACTER SET utf8  COLLATE utf8_general_ci  NOT NULL  DEFAULT \'\'')
			}
		}
	}
}
