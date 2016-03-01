package core
databaseChangeLog = {

	changeSet(author: "aapeli", id: "1452621480180-100") {
		sql("""
				INSERT INTO module VALUE(
					217, '2', '3', 'com.unifina.signalpath.utils.RateLimit', 'RateLimit', 'GenericModule', NULL, 'module', '1', '{\\"params\\":{\\"rate\\":\\"How many messages are let through in given time\\",\\"timeInMillis\\":\\"The time in milliseconds, in which the given number of messages are let through\\"},\\"paramNames\\":[\\"rate\\",\\"timeInMillis\\"],\\"inputs\\":{\\"in\\":\\"Input\\"},\\"inputNames\\":[\\"in\\"],\\"outputs\\":{\\"limitExceeded?\\":\\"Outputs 1 if the message was blocked and 0 if it wasn\\'t\\",\\"out\\":\\"Outputs the input value if it wasn\\'t blocked\\"},\\"outputNames\\":[\\"limitExceeded?\\",\\"out\\"],\\"helpText\\":\\"<p>The RateLimit module lets through n messages in t milliseconds. Then module just blocks the rest which do not fit in the window.</p>\\\\n\\"}', NULL, NULL
				);
		""")
	}

}