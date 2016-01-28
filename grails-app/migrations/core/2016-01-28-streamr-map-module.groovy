package core

databaseChangeLog = {

	changeSet(author: "aapeli", id: "145262149000-36") {
		sql("""
			INSERT INTO module value(
				Null,
				'1',
				'13',
				'com.unifina.signalpath.charts.Map',
				'Map',
				'MapModule',
				NULL,
				'module',
				'1',
				NULL,
				NULL,
				'streamr-map'
			);
		""")
	}

}
