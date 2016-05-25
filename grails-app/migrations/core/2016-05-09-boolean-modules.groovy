package core

databaseChangeLog = {

	changeSet(author: "aapeli", id: "add-convert-module-type-and-boolean-to-number-module") {
		sql("""
			UPDATE module_category SET parent_id = null WHERE id = 10;
		""")
		sql("""
			INSERT INTO module (version, category_id, implementing_class, name, js_module, hide, type, module_package_id, json_help, alternative_names, webcomponent)
			VALUES (0, 10, "com.unifina.signalpath.convert.BooleanToNumber", "BooleanToNumber", "GenericModule", null, "module", 1, "", null, null);
		""")
		sql("""
			INSERT INTO module (version, category_id, implementing_class, name, js_module, hide, type, module_package_id, json_help, alternative_names, webcomponent)
			VALUES (0, 10, "com.unifina.signalpath.bool.BooleanConstant", "BooleanConstant", "GenericModule", null, "module", 1, "", null, null);
		""")
	}

}
