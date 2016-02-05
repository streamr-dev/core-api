package core

databaseChangeLog = {

	changeSet(author: "aapeli", id: "145262149100-36") {
		sql("""
			INSERT INTO module_category VALUE(
				50,
				0,
				"Color",
				1,
				3,
				1,
				NULL
			);
		""")
		sql("""
			INSERT INTO module value(
				NULL,
				1,
				50,
				'com.unifina.signalpath.color.ColorConstant',
				'ColorConstant',
				'GenericModule',
				NULL,
				'module',
				1,
				NULL,
				NULL,
				NULL
			);
		""")
		sql("""
			INSERT INTO module value(
				NULL,
				1,
				50,
				'com.unifina.signalpath.color.Gradient',
				'Gradient',
				'GenericModule',
				NULL,
				'module',
				1,
				NULL,
				NULL,
				NULL
			);
		""")
	}

}
