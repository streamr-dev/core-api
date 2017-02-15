package core
databaseChangeLog = {
	changeSet(author: "eric", id: "update-map-module") {
		update(tableName: "module") {
			column(name: "implementing_class", value: 'com.unifina.signalpath.charts.GeographicalMapModule')
			where("id = 214")
		}
	}
}