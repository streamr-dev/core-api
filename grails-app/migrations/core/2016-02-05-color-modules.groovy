package core

databaseChangeLog = {

	changeSet(author: "aapeli", id: "145262149100-36") {
		sql("""
			INSERT INTO module_category (id, version, hide, module_package_id, name, parent_id, sort_order)
			VALUE(
				50,
				0,
				NULL,
				1,
				"Color",
				3,
				1
			);
		""")
		sql("""
			INSERT INTO module (id, version, category_id, implementing_class, name, js_module, hide, type, module_package_id, json_help, alternative_names, webcomponent)
			VALUES (
				215,
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
			), (
				216,
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
