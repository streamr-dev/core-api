
package core

databaseChangeLog = {
	changeSet(author: "aapeli", id: "canvas-spec-test-canvas", context: "test") {
		sql("""
			UPDATE canvas
			SET json = '{"name":"CanvasSpec test loading a SignalPath","modules":[{"id":520,"clearState":false,"inputs":[{"canConnect":true,"connected":false,"canHaveInitialValue":true,"type":"Double","requiresConnection":true,"canToggleDrivingInput":true,"id":"myId_3_1465818123072","feedback":false,"canBeFeedback":true,"drivingInput":true,"name":"in1","longName":"Add.in1","acceptedTypes":["Double"]},{"canConnect":true,"connected":false,"canHaveInitialValue":true,"type":"Double","requiresConnection":true,"canToggleDrivingInput":true,"id":"myId_3_1465818123081","feedback":false,"canBeFeedback":true,"drivingInput":true,"name":"in2","longName":"Add.in2","acceptedTypes":["Double"]},{"canConnect":true,"connected":false,"jsClass":"VariadicInput","canHaveInitialValue":true,"type":"Double","requiresConnection":false,"canToggleDrivingInput":true,"id":"myId_3_1465818123087","feedback":false,"canBeFeedback":true,"variadic":{"isLast":true,"index":3},"name":"endpoint-1465818123055","drivingInput":true,"longName":"Add.endpoint-1465818123055","displayName":"in3","acceptedTypes":["Double"]}],"hash":3,"canClearState":true,"layout":{"position":{"left":"48px","top":"22px"}},"name":"Add","params":[],"type":"module","outputs":[{"id":"myId_3_1465818123092","canConnect":true,"canBeNoRepeat":true,"name":"sum","connected":false,"longName":"Add.sum","noRepeat":true,"type":"Double"}],"jsModule":"GenericModule"}],"settings":{"editorState":{"runTab":"#tab-historical"},"speed":"0","timeOfDayFilter":{"timeOfDayStart":"00:00:00","timeOfDayEnd":"23:59:00"},"endDate":"2015-07-03","beginDate":"2015-07-02"},"hasExports":false,"uiChannel":{"id":"MEvHi5WKRUuBc3K6BZWXxA","webcomponent":null,"name":"Notifications"}}'
			WHERE id = "Fqpkl9wwTJS04p3NzsXphA";
		""")
	}
}