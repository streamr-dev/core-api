package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "rm-old-ethereum-call-module") {
		grailsChange {
			change {
				sql.execute('DELETE FROM module WHERE id = 1020')
			}
		}
	}
	changeSet(author: "jtakalai", id: "rm-old-solidity-module") {
		grailsChange {
			change {
				sql.execute('DELETE FROM module WHERE id = 1021')
			}
		}
	}
}
