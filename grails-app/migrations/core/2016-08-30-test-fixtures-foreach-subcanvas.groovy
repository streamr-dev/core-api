package core
databaseChangeLog = {
	changeSet(author: "henri", id: "2016-08-30-test-fixtures-foreach-subcanvas", context: "test") {
		sql("""
			INSERT INTO `canvas` (`id`, `version`, `adhoc`, `date_created`, `example`, `has_exports`, `json`, `last_updated`, `name`, `request_url`, `runner`, `serialization_time`, `serialized`, `server`, `state`, `user_id`)
			VALUES
			\t('VWo3BDECTASlAdtZk7QeeQ', 7, 00000000, '2016-08-29 13:49:09', 00000000, 00000001, '{\\"name\\":\\"SubCanvasSpec-sub\\",\\"modules\\":[{\\"hash\\":4,\\"tableConfig\\":{\\"headers\\":[\\"timestamp\\",\\"out\\"]},\\"uiChannel\\":{\\"id\\":\\"bL2DweKqQ7mPY3LweMo4vQ\\",\\"webcomponent\\":\\"streamr-table\\",\\"name\\":\\"Table\\"},\\"params\\":[],\\"type\\":\\"module event-table-module\\",\\"id\\":527,\\"inputs\\":[{\\"canConnect\\":true,\\"export\\":false,\\"connected\\":true,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"EHuH-QjCTtustNwc5iclzQ\\",\\"id\\":\\"VDDk7c6uQqCUHYEgnBGptw\\",\\"variadic\\":{\\"isLast\\":false,\\"index\\":1},\\"name\\":\\"endpoint-1472478499156\\",\\"drivingInput\\":true,\\"longName\\":\\"Table.endpoint-1472478499156\\",\\"displayName\\":\\"in1\\",\\"acceptedTypes\\":[\\"Object\\"],\\"value\\":\\"STR\\"},{\\"canConnect\\":true,\\"export\\":false,\\"connected\\":false,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"id\\":\\"myId_4_1472545419295\\",\\"variadic\\":{\\"isLast\\":true,\\"index\\":2},\\"name\\":\\"endpoint1472545419295\\",\\"drivingInput\\":true,\\"displayName\\":\\"in2\\",\\"acceptedTypes\\":[\\"Object\\"],\\"longName\\":\\"Table.endpoint1472545419295\\"}],\\"canClearState\\":true,\\"layout\\":{\\"position\\":{\\"left\\":\\"497px\\",\\"top\\":\\"257px\\"}},\\"name\\":\\"Table\\",\\"outputs\\":[],\\"jsModule\\":\\"TableModule\\",\\"options\\":{\\"uiResendLast\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"maxRows\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"uiResendAll\\":{\\"value\\":false,\\"type\\":\\"boolean\\"}}},{\\"id\\":145,\\"inputs\\":[{\\"id\\":\\"LUaoljnHRmC0i0AEYWO_Gw\\",\\"canConnect\\":true,\\"export\\":true,\\"name\\":\\"label\\",\\"connected\\":false,\\"drivingInput\\":true,\\"longName\\":\\"Label.label\\",\\"type\\":\\"Object\\",\\"acceptedTypes\\":[\\"Object\\"],\\"requiresConnection\\":true,\\"canToggleDrivingInput\\":false}],\\"hash\\":5,\\"canClearState\\":false,\\"layout\\":{\\"position\\":{\\"left\\":\\"189px\\",\\"top\\":\\"94px\\"}},\\"name\\":\\"Label\\",\\"params\\":[],\\"uiChannel\\":{\\"id\\":\\"SqUC_6rBSVqLs9HHMPaSRg\\",\\"webcomponent\\":\\"streamr-label\\",\\"name\\":\\"Label\\"},\\"type\\":\\"module dashboard\\",\\"outputs\\":[],\\"jsModule\\":\\"LabelModule\\"},{\\"id\\":19,\\"inputs\\":[],\\"hash\\":6,\\"canClearState\\":true,\\"layout\\":{\\"position\\":{\\"left\\":\\"186px\\",\\"top\\":\\"227px\\"}},\\"name\\":\\"ConstantText\\",\\"params\\":[{\\"id\\":\\"EVfZN35MQMOthQ4rN8mgZQ\\",\\"canConnect\\":true,\\"export\\":true,\\"name\\":\\"str\\",\\"connected\\":false,\\"drivingInput\\":true,\\"longName\\":\\"ConstantText.str\\",\\"value\\":\\"STR\\",\\"type\\":\\"String\\",\\"defaultValue\\":\\"STR\\",\\"acceptedTypes\\":[\\"String\\"],\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false}],\\"type\\":\\"module\\",\\"outputs\\":[{\\"id\\":\\"EHuH-QjCTtustNwc5iclzQ\\",\\"canConnect\\":true,\\"canBeNoRepeat\\":true,\\"name\\":\\"out\\",\\"connected\\":true,\\"longName\\":\\"ConstantText.out\\",\\"value\\":\\"STR\\",\\"noRepeat\\":true,\\"type\\":\\"String\\",\\"targets\\":[\\"VDDk7c6uQqCUHYEgnBGptw\\"]}],\\"jsModule\\":\\"GenericModule\\"}],\\"settings\\":{\\"editorState\\":{\\"runTab\\":\\"#tab-historical\\"},\\"speed\\":\\"0\\",\\"timeOfDayFilter\\":{\\"timeOfDayStart\\":\\"00:00:00\\",\\"timeOfDayEnd\\":\\"23:59:00\\"},\\"endDate\\":\\"2016-08-29\\",\\"beginDate\\":\\"2016-08-29\\"},\\"hasExports\\":true,\\"uiChannel\\":{\\"id\\":\\"4AOpGMe6SYecTgPrsDR4pQ\\",\\"webcomponent\\":null,\\"name\\":\\"Notifications\\"}}', '2016-08-30 08:23:49', 'SubCanvasSpec-sub', NULL, NULL, NULL, NULL, NULL, 'stopped', 1);
		""")
		sql("""
			INSERT INTO `canvas` (`id`, `version`, `adhoc`, `date_created`, `example`, `has_exports`, `json`, `last_updated`, `name`, `request_url`, `runner`, `serialization_time`, `serialized`, `server`, `state`, `user_id`)
			VALUES
			\t('eyescpGFRiKzr9WxU2k0Yw', 81, 00000000, '2016-08-29 13:50:12', 00000000, 00000001, '{\\"name\\":\\"SubCanvasSpec-top\\",\\"modules\\":[{\\"hash\\":1,\\"textFieldValue\\":\\"\\",\\"uiChannel\\":{\\"id\\":\\"aBH9ZPAxTE2XvgJ7bKgQXg\\",\\"webcomponent\\":\\"streamr-text-field\\",\\"name\\":\\"TextField\\"},\\"params\\":[],\\"type\\":\\"module\\",\\"id\\":220,\\"textFieldHeight\\":76,\\"inputs\\":[],\\"textFieldWidth\\":174,\\"canClearState\\":true,\\"layout\\":{\\"position\\":{\\"left\\":\\"103px\\",\\"top\\":\\"118px\\"}},\\"name\\":\\"TextField\\",\\"outputs\\":[{\\"id\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"canConnect\\":true,\\"canBeNoRepeat\\":true,\\"name\\":\\"out\\",\\"connected\\":true,\\"longName\\":\\"TextField.out\\",\\"noRepeat\\":true,\\"type\\":\\"String\\",\\"targets\\":[\\"tfG-zZItTDmS4G7CdTzp7A\\",\\"32yzaprIS7S64pfbQOQFCg\\",\\"69oKfPEuQwayNbAuXv8TSw\\",\\"c24ESIiATW67SYdlSovPTw\\",\\"XwspREeeTAWeGL7HBKtzKQ\\"]}],\\"widget\\":\\"StreamrTextField\\",\\"options\\":{\\"uiResendLast\\":{\\"value\\":1,\\"type\\":\\"int\\"},\\"uiResendAll\\":{\\"value\\":false,\\"type\\":\\"boolean\\"}},\\"jsModule\\":\\"InputModule\\"},{\\"id\\":223,\\"canRefresh\\":true,\\"inputs\\":[{\\"canConnect\\":true,\\"connected\\":true,\\"canHaveInitialValue\\":true,\\"type\\":\\"String\\",\\"requiresConnection\\":true,\\"canToggleDrivingInput\\":true,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"feedback\\":false,\\"id\\":\\"32yzaprIS7S64pfbQOQFCg\\",\\"canBeFeedback\\":true,\\"initialValue\\":null,\\"name\\":\\"key\\",\\"drivingInput\\":true,\\"longName\\":\\"ForEach.key\\",\\"value\\":\\"älälälälä\\",\\"acceptedTypes\\":[\\"String\\"]},{\\"canConnect\\":true,\\"export\\":true,\\"connected\\":true,\\"type\\":\\"Object\\",\\"requiresConnection\\":true,\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"id\\":\\"69oKfPEuQwayNbAuXv8TSw\\",\\"name\\":\\"label\\",\\"drivingInput\\":true,\\"value\\":\\"älälälälä\\",\\"longName\\":\\"ForEach.label\\",\\"acceptedTypes\\":[\\"Object\\"]}],\\"hash\\":4,\\"canClearState\\":true,\\"name\\":\\"ForEach\\",\\"layout\\":{\\"position\\":{\\"left\\":\\"612px\\",\\"top\\":\\"82px\\"}},\\"params\\":[{\\"canConnect\\":true,\\"updateOnChange\\":true,\\"possibleValues\\":[{\\"value\\":\\"eyescpGFRiKzr9WxU2k0Yw\\",\\"name\\":\\"SubCanvasSpec-top\\"},{\\"value\\":\\"VWo3BDECTASlAdtZk7QeeQ\\",\\"name\\":\\"SubCanvasSpec-sub\\"}],\\"connected\\":false,\\"type\\":\\"Canvas\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":true,\\"id\\":\\"_iIuDBrCSoqhuX_2Vf8_og\\",\\"drivingInput\\":false,\\"name\\":\\"canvas\\",\\"value\\":\\"VWo3BDECTASlAdtZk7QeeQ\\",\\"longName\\":\\"ForEach.canvas\\",\\"defaultValue\\":\\"VWo3BDECTASlAdtZk7QeeQ\\",\\"acceptedTypes\\":[\\"Canvas\\"]},{\\"id\\":\\"c24ESIiATW67SYdlSovPTw\\",\\"canConnect\\":true,\\"export\\":true,\\"name\\":\\"str\\",\\"connected\\":true,\\"drivingInput\\":true,\\"longName\\":\\"ForEach.str\\",\\"value\\":\\"STR\\",\\"type\\":\\"String\\",\\"defaultValue\\":\\"STR\\",\\"acceptedTypes\\":[\\"String\\"],\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\"}],\\"canvasesByKey\\":{},\\"outputs\\":[{\\"canBeNoRepeat\\":true,\\"canConnect\\":true,\\"id\\":\\"mJs0o416T7ukb4jpyTs_Aw\\",\\"connected\\":false,\\"name\\":\\"key\\",\\"longName\\":\\"ForEach.key\\",\\"noRepeat\\":true,\\"type\\":\\"String\\"},{\\"canConnect\\":true,\\"id\\":\\"-iIT8BZ_T6ONKXmlRVIwxQ\\",\\"connected\\":false,\\"name\\":\\"map\\",\\"longName\\":\\"ForEach.map\\",\\"type\\":\\"Map\\"}],\\"type\\":\\"module\\",\\"jsModule\\":\\"ForEachModule\\"},{\\"hash\\":5,\\"tableConfig\\":{\\"headers\\":[\\"timestamp\\",\\"out\\"]},\\"uiChannel\\":{\\"id\\":\\"cny8lZ28TvKtZ-SLPs32VQ\\",\\"webcomponent\\":\\"streamr-table\\",\\"name\\":\\"Table\\"},\\"params\\":[],\\"type\\":\\"module event-table-module\\",\\"id\\":527,\\"inputs\\":[{\\"canConnect\\":true,\\"connected\\":true,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"id\\":\\"XwspREeeTAWeGL7HBKtzKQ\\",\\"variadic\\":{\\"isLast\\":false,\\"index\\":1},\\"name\\":\\"endpoint-1472545463031\\",\\"drivingInput\\":true,\\"longName\\":\\"Table.endpoint-1472545463031\\",\\"displayName\\":\\"in1\\",\\"acceptedTypes\\":[\\"Object\\"]},{\\"canConnect\\":true,\\"connected\\":false,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"id\\":\\"myId_5_1472545465692\\",\\"drivingInput\\":true,\\"name\\":\\"endpoint1472545465692\\",\\"variadic\\":{\\"isLast\\":true,\\"index\\":2},\\"longName\\":\\"Table.endpoint1472545465692\\",\\"displayName\\":\\"in2\\",\\"acceptedTypes\\":[\\"Object\\"]}],\\"canClearState\\":true,\\"layout\\":{\\"position\\":{\\"left\\":\\"599px\\",\\"top\\":\\"286px\\"}},\\"name\\":\\"Table\\",\\"outputs\\":[],\\"jsModule\\":\\"TableModule\\",\\"options\\":{\\"uiResendLast\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"maxRows\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"uiResendAll\\":{\\"value\\":false,\\"type\\":\\"boolean\\"}}}],\\"settings\\":{\\"editorState\\":{\\"runTab\\":\\"#tab-realtime\\"},\\"speed\\":\\"0\\",\\"timeOfDayFilter\\":{\\"timeOfDayStart\\":\\"00:00:00\\",\\"timeOfDayEnd\\":\\"23:59:00\\"},\\"endDate\\":\\"2016-08-29\\",\\"beginDate\\":\\"2016-08-29\\"},\\"hasExports\\":true,\\"uiChannel\\":{\\"id\\":\\"V_GXmKCXTtOxMICLYhq4bg\\",\\"webcomponent\\":null,\\"name\\":\\"Notifications\\"}}', '2016-08-30 09:01:15', 'SubCanvasSpec-top', 'http://192.168.10.137:8081/unifina-core/api/v1/canvases/eyescpGFRiKzr9WxU2k0Yw', 's-1472547524992', '2016-08-30 09:02:10', X'', '192.168.10.137', 'stopped', 1);
		""")
	}
}
