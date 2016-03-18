package core

databaseChangeLog = {

	changeSet(author: "aapeli", id: "145262149000-36") {
		sql("""
			INSERT INTO module(id, version, category_id, implementing_class, name, js_module, hide, type, module_package_id, json_help, alternative_names, webcomponent)
			VALUE(
				214,
				'1',
				'13',
				'com.unifina.signalpath.charts.MapModule',
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
