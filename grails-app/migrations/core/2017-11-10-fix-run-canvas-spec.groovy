package core
databaseChangeLog = {
	changeSet(author: "eric", id: "fix-run-canvas-spec", context: "test") {
		insert(tableName: "stream") {
			column(name: "id", value: "run-canvas-spec")
			column(name: "name", value: "run-canvas-spec")
			column(name: "description", value: "Stream for integration test RunCanvasSpec")
			column(name: "feed_id", valueNumeric: 7)
			column(name: "version", valueNumeric: 0)
			column(name: "class", value: "com.unifina.domain.data.Stream")
			column(name: "config", value: '{"fields": [{"name": "numero", "type": "number"}, {"name": "areWeDoneYet", "type": "boolean"}]}')
			column(name: "user_id", valueNumeric: 1)
			column(name: "date_created", value: "2017-11-10 16:43:01")
			column(name: "last_updated", value: "2017-11-10 16:43:01")
			column(name: "partitions", valueNumeric: 1)
			column(name: "ui_channel", valueNumeric: 0)
		}

		insert(tableName: "canvas") {
			column(name: "id", value: "run-canvas-spec")
			column(name: "version", valueNumeric: 0)
			column(name: "adhoc", valueNumeric: 0)
			column(name: "date_created", value: "2017-11-10 16:43:01")
			column(name: "example", valueNumeric: 0)
			column(name: "has_exports", valueNumeric: 0)
			column(name: "last_updated", value: "2017-11-10 16:43:01")
			column(name: "name", value: "run-canvas-spec Canvas")
			column(name: "state", value: "stopped")
			column(name: "user_id", valueNumeric: 1)
			column(name: "json", value: '{\n' +
				'  "name": "run-canvas-spec Canvas",\n' +
				'  "settings": {\n' +
				'    "adhoc": true,\n' +
				'    "live": true,\n' +
				'    "speed": "0"\n' +
				'  },\n' +
				'  "modules": [\n' +
				'    {\n' +
				'      "canClearState": true,\n' +
				'      "clearState": false,\n' +
				'      "hash": 0,\n' +
				'      "id": 147,\n' +
				'      "inputs": [],\n' +
				'      "jsModule": "GenericModule",\n' +
				'      "layout": {\n' +
				'        "position": {\n' +
				'          "left": "98px",\n' +
				'          "top": "132px"\n' +
				'        }\n' +
				'      },\n' +
				'      "name": "Stream",\n' +
				'      "outputs": [\n' +
				'        {\n' +
				'          "canBeNoRepeat": true,\n' +
				'          "canConnect": true,\n' +
				'          "connected": true,\n' +
				'          "id": "myId_0_1453815974997",\n' +
				'          "longName": "Stream.numero",\n' +
				'          "name": "numero",\n' +
				'          "noRepeat": false,\n' +
				'          "targets": [\n' +
				'            "myId_2_1453816012451"\n' +
				'          ],\n' +
				'          "type": "Double"\n' +
				'        },\n' +
				'        {\n' +
				'          "canBeNoRepeat": true,\n' +
				'          "canConnect": true,\n' +
				'          "connected": false,\n' +
				'          "id": "myId_0_1453815975004",\n' +
				'          "longName": "Stream.areWeDoneYet",\n' +
				'          "name": "areWeDoneYet",\n' +
				'          "noRepeat": false,\n' +
				'          "type": "Double"\n' +
				'        }\n' +
				'      ],\n' +
				'      "params": [\n' +
				'        {\n' +
				'          "acceptedTypes": [\n' +
				'            "Stream",\n' +
				'            "String"\n' +
				'          ],\n' +
				'          "canConnect": true,\n' +
				'          "canToggleDrivingInput": false,\n' +
				'          "checkModuleId": true,\n' +
				'          "connected": false,\n' +
				'          "defaultValue": null,\n' +
				'          "drivingInput": false,\n' +
				'          "feed": 7,\n' +
				'          "id": "myId_0_1453815974975",\n' +
				'          "longName": "Stream.stream",\n' +
				'          "name": "stream",\n' +
				'          "requiresConnection": false,\n' +
				'          "streamName": "run-canvas-spec-stream",\n' +
				'          "type": "Stream",\n' +
				'          "value": "run-canvas-spec"\n' +
				'        }\n' +
				'      ],\n' +
				'      "type": "module"\n' +
				'    },\n' +
				'    {\n' +
				'      "canClearState": true,\n' +
				'      "clearState": false,\n' +
				'      "hash": 1,\n' +
				'      "id": 53,\n' +
				'      "inputs": [\n' +
				'        {\n' +
				'          "acceptedTypes": [\n' +
				'            "Double"\n' +
				'          ],\n' +
				'          "canBeFeedback": true,\n' +
				'          "canConnect": true,\n' +
				'          "canHaveInitialValue": true,\n' +
				'          "canToggleDrivingInput": true,\n' +
				'          "connected": true,\n' +
				'          "drivingInput": true,\n' +
				'          "feedback": false,\n' +
				'          "id": "myId_1_1453815990310",\n' +
				'          "longName": "Sum.in",\n' +
				'          "name": "in",\n' +
				'          "requiresConnection": true,\n' +
				'          "sourceId": "myId_2_1453816012459",\n' +
				'          "type": "Double"\n' +
				'        }\n' +
				'      ],\n' +
				'      "jsModule": "GenericModule",\n' +
				'      "layout": {\n' +
				'        "position": {\n' +
				'          "left": "631px",\n' +
				'          "top": "197px"\n' +
				'        }\n' +
				'      },\n' +
				'      "name": "Sum",\n' +
				'      "outputs": [\n' +
				'        {\n' +
				'          "canBeNoRepeat": true,\n' +
				'          "canConnect": true,\n' +
				'          "connected": false,\n' +
				'          "id": "myId_1_1453815990315",\n' +
				'          "longName": "Sum.out",\n' +
				'          "name": "out",\n' +
				'          "noRepeat": true,\n' +
				'          "type": "Double"\n' +
				'        }\n' +
				'      ],\n' +
				'      "params": [\n' +
				'        {\n' +
				'          "acceptedTypes": [\n' +
				'            "Double"\n' +
				'          ],\n' +
				'          "canConnect": true,\n' +
				'          "canToggleDrivingInput": true,\n' +
				'          "connected": false,\n' +
				'          "defaultValue": 0,\n' +
				'          "drivingInput": false,\n' +
				'          "id": "myId_1_1453815990298",\n' +
				'          "longName": "Sum.windowLength",\n' +
				'          "name": "windowLength",\n' +
				'          "requiresConnection": false,\n' +
				'          "type": "Double",\n' +
				'          "value": 0\n' +
				'        },\n' +
				'        {\n' +
				'          "acceptedTypes": [\n' +
				'            "Double"\n' +
				'          ],\n' +
				'          "canConnect": true,\n' +
				'          "canToggleDrivingInput": true,\n' +
				'          "connected": false,\n' +
				'          "defaultValue": 1,\n' +
				'          "drivingInput": false,\n' +
				'          "id": "myId_1_1453815990304",\n' +
				'          "longName": "Sum.minSamples",\n' +
				'          "name": "minSamples",\n' +
				'          "requiresConnection": false,\n' +
				'          "type": "Double",\n' +
				'          "value": 1\n' +
				'        }\n' +
				'      ],\n' +
				'      "type": "module"\n' +
				'    },\n' +
				'    {\n' +
				'      "canClearState": true,\n' +
				'      "clearState": false,\n' +
				'      "hash": 2,\n' +
				'      "id": 1,\n' +
				'      "inputs": [\n' +
				'        {\n' +
				'          "acceptedTypes": [\n' +
				'            "Double"\n' +
				'          ],\n' +
				'          "canBeFeedback": true,\n' +
				'          "canConnect": true,\n' +
				'          "canHaveInitialValue": true,\n' +
				'          "canToggleDrivingInput": true,\n' +
				'          "connected": true,\n' +
				'          "drivingInput": true,\n' +
				'          "feedback": false,\n' +
				'          "id": "myId_2_1453816012451",\n' +
				'          "longName": "Multiply.A",\n' +
				'          "name": "A",\n' +
				'          "requiresConnection": true,\n' +
				'          "sourceId": "myId_0_1453815974997",\n' +
				'          "type": "Double"\n' +
				'        },\n' +
				'        {\n' +
				'          "acceptedTypes": [\n' +
				'            "Double"\n' +
				'          ],\n' +
				'          "canBeFeedback": true,\n' +
				'          "canConnect": true,\n' +
				'          "canHaveInitialValue": true,\n' +
				'          "canToggleDrivingInput": true,\n' +
				'          "connected": true,\n' +
				'          "drivingInput": true,\n' +
				'          "feedback": false,\n' +
				'          "id": "myId_2_1453816012456",\n' +
				'          "initialValue": 2.0,\n' +
				'          "longName": "Multiply.B",\n' +
				'          "name": "B",\n' +
				'          "requiresConnection": true,\n' +
				'          "sourceId": "myId_3_1453816023857",\n' +
				'          "type": "Double"\n' +
				'        }\n' +
				'      ],\n' +
				'      "jsModule": "GenericModule",\n' +
				'      "layout": {\n' +
				'        "position": {\n' +
				'          "left": "341px",\n' +
				'          "top": "202px"\n' +
				'        }\n' +
				'      },\n' +
				'      "name": "Multiply",\n' +
				'      "outputs": [\n' +
				'        {\n' +
				'          "canBeNoRepeat": true,\n' +
				'          "canConnect": true,\n' +
				'          "connected": true,\n' +
				'          "id": "myId_2_1453816012459",\n' +
				'          "longName": "Multiply.A*B",\n' +
				'          "name": "A*B",\n' +
				'          "noRepeat": true,\n' +
				'          "targets": [\n' +
				'            "myId_1_1453815990310"\n' +
				'          ],\n' +
				'          "type": "Double"\n' +
				'        }\n' +
				'      ],\n' +
				'      "params": [],\n' +
				'      "type": "module"\n' +
				'    },\n' +
				'    {\n' +
				'      "canClearState": true,\n' +
				'      "clearState": false,\n' +
				'      "hash": 3,\n' +
				'      "id": 5,\n' +
				'      "inputs": [],\n' +
				'      "jsModule": "GenericModule",\n' +
				'      "layout": {\n' +
				'        "position": {\n' +
				'          "left": "89px",\n' +
				'          "top": "303px"\n' +
				'        }\n' +
				'      },\n' +
				'      "name": "Constant",\n' +
				'      "outputs": [\n' +
				'        {\n' +
				'          "canBeNoRepeat": true,\n' +
				'          "canConnect": true,\n' +
				'          "connected": true,\n' +
				'          "id": "myId_3_1453816023857",\n' +
				'          "longName": "Constant.out",\n' +
				'          "name": "out",\n' +
				'          "noRepeat": true,\n' +
				'          "targets": [\n' +
				'            "myId_2_1453816012456"\n' +
				'          ],\n' +
				'          "type": "Double"\n' +
				'        }\n' +
				'      ],\n' +
				'      "params": [\n' +
				'        {\n' +
				'          "acceptedTypes": [\n' +
				'            "Double"\n' +
				'          ],\n' +
				'          "canConnect": true,\n' +
				'          "canToggleDrivingInput": true,\n' +
				'          "connected": false,\n' +
				'          "defaultValue": 0.0,\n' +
				'          "drivingInput": true,\n' +
				'          "id": "myId_3_1453816023852",\n' +
				'          "longName": "Constant.constant",\n' +
				'          "name": "constant",\n' +
				'          "requiresConnection": false,\n' +
				'          "type": "Double",\n' +
				'          "value": 2.0\n' +
				'        }\n' +
				'      ],\n' +
				'      "type": "module"\n' +
				'    }\n' +
				'  ]\n' +
				'}')
		}
	}
}
