package core
databaseChangeLog = {

	changeSet(author: "aapeli", id: "1452621480375-1") {
		sql("""
			INSERT INTO module VALUE(
				NULL, '3', '28', 'com.unifina.signalpath.time.Scheduler', 'Scheduler', 'SchedulerModule', NULL, 'module', '1', '{\\"params\\":{},\\"paramNames\\":[],\\"inputs\\":{},\\"inputNames\\":[],\\"outputs\\":{\\"value\\":\\"The value from a active rule or the default value\\"},\\"outputNames\\":[\\"value\\"],\\"helpText\\":\\"<p>Outputs a certain value at a certain time.&nbsp;E.g. Every day from 10:00 to 14:00 the module outputs value 1&nbsp;and otherwise value 0.<br />\\\\nIf more than one rule are active at the same time, the value from the rule with the highest priority (the highest rule in the list) is sent.<br />\\\\nIf no rule is active,&nbsp;the default value will be sent out.&nbsp;</p>\\\\n\\"}', NULL, NULL
			);
		""")
	}

}