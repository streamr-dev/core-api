package core

databaseChangeLog = {

	changeSet(author: "henri", id: "constant-list") {
		sql("""
			INSERT INTO `module_category` (`id`, `version`, `name`, `sort_order`, `parent_id`, `module_package_id`, `hide`)
			VALUES
			(52, '0', 'List', '142', NULL, '1', NULL);
		""")

		sql("""
			INSERT INTO `module` (`id`, `version`, `category_id`, `implementing_class`, `name`, `js_module`, `hide`, `type`, `module_package_id`, `json_help`, `alternative_names`, `webcomponent`)
			VALUES
			(802, 1, 52, 'com.unifina.signalpath.list.ConstantList', 'ConstantList', 'GenericModule', NULL, 'module', 1, '{\\"params\\":{},\\"paramNames\\":[],\\"inputs\\":{},\\"inputNames\\":[],\\"outputs\\":{},\\"outputNames\\":[],\\"helpText\\":\\"<p>This module allows you to manually enter a constant List object.</p>\\\\n\\"}', 'ListConstant', NULL);
		""")
	}
}
