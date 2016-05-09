package core

databaseChangeLog = {

	changeSet(author: "aapeli", id: "add-convert-module-type-and-boolean-to-number-module") {
		sql("""
			INSERT INTO module_category (id, version, name, sort_order, parent_id, module_package_id, hide)
			VALUES (200, 0, "Convert", 150, null, 1, null);
		""")
		sql("""
			INSERT INTO module (version, category_id, implementing_class, name, js_module, hide, type, module_package_id, json_help, alternative_names, webcomponent)
			VALUES (0, 200, "com.unifina.signalpath.convert.BooleanToNumber", "BooleanToNumber", "GenericModule", null, "module", 1, "", null, null);
		""")
		sql("""
			INSERT INTO module (version, category_id, implementing_class, name, js_module, hide, type, module_package_id, json_help, alternative_names, webcomponent)
			VALUES (0, 10, "com.unifina.signalpath.bool.BooleanConstant", "BooleanConstant", "GenericModule", null, "module", 1, "", null, null);
		""")
	}

}
