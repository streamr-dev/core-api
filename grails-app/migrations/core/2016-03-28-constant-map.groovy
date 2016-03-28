package core

databaseChangeLog = {
	changeSet(author: "henri", id: "constant-map") {
		sql("""
			INSERT INTO `module` (`id`, `version`, `category_id`, `implementing_class`, `name`, `js_module`, `hide`, `type`, `module_package_id`, `json_help`, `alternative_names`, `webcomponent`)
			VALUES
			(800, 1, 51, 'com.unifina.signalpath.map.ConstantMap', 'ConstantMap', 'GenericModule', NULL, 'module', 1, '{\\"params\\":{},\\"paramNames\\":[],\\"inputs\\":{},\\"inputNames\\":[],\\"outputs\\":{},\\"outputNames\\":[],\\"helpText\\":\\"<p>This module allows you to enter a constant Map object, which is a set of key-value pairs. It can be connected to any Map input in Streamr - for example, to set headers on the HTTP module.</p>\\\\n\\"}', 'MapConstant', NULL);
		""")
	}

	changeSet(author: "henri", id: "consant-naming-coherence") {
		// Rename ColorConstant to ConstantColor
		sql("UPDATE `module` SET `name` = 'ConstantColor', `alternative_names` = 'ColorConstant, Color' WHERE `id` = '215';")
		// Add reverse alternative names to ConstantText
		sql("UPDATE `module` SET `alternative_names` = 'TextConstant, ConstantString, StringConstant, String' WHERE `id` = '19';")
	}

}
