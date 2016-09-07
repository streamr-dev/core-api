package core
databaseChangeLog = {
	changeSet(author: "henri", id: "2016-08-30-test-fixtures-foreach-subcanvas", context: "test") {
		sql("""
INSERT INTO `canvas` (`id`, `version`, `adhoc`, `date_created`, `example`, `has_exports`, `json`, `last_updated`, `name`, `request_url`, `runner`, `serialization_time`, `serialized`, `server`, `state`, `user_id`)
VALUES
\t('VWo3BDECTASlAdtZk7QeeQ', 9, 00000000, '2016-08-29 13:49:09', 00000000, 00000001, '{\\"name\\":\\"SubCanvasSpec-sub\\",\\"modules\\":[{\\"id\\":145,\\"inputs\\":[{\\"canConnect\\":true,\\"id\\":\\"LUaoljnHRmC0i0AEYWO_Gw\\",\\"export\\":true,\\"drivingInput\\":true,\\"connected\\":false,\\"name\\":\\"label\\",\\"longName\\":\\"Label.label\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":true,\\"acceptedTypes\\":[\\"Object\\"],\\"canToggleDrivingInput\\":false}],\\"hash\\":5,\\"canClearState\\":false,\\"name\\":\\"Label\\",\\"layout\\":{\\"position\\":{\\"left\\":\\"189px\\",\\"top\\":\\"94px\\"}},\\"uiChannel\\":{\\"id\\":\\"SqUC_6rBSVqLs9HHMPaSRg\\",\\"webcomponent\\":\\"streamr-label\\",\\"name\\":\\"Label\\"},\\"params\\":[],\\"outputs\\":[],\\"type\\":\\"module dashboard\\",\\"jsModule\\":\\"LabelModule\\"},{\\"hash\\":7,\\"tableConfig\\":{\\"headers\\":[\\"timestamp\\",\\"in1\\"]},\\"uiChannel\\":{\\"id\\":\\"FIwXENHwQye40m9IxV1TmA\\",\\"webcomponent\\":\\"streamr-table\\",\\"name\\":\\"Table\\"},\\"params\\":[],\\"type\\":\\"module event-table-module\\",\\"id\\":527,\\"inputs\\":[{\\"canConnect\\":true,\\"export\\":true,\\"connected\\":false,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"id\\":\\"ep_qMiYZPCeR3K1OJQIyP_GXA\\",\\"variadic\\":{\\"isLast\\":false,\\"index\\":1},\\"name\\":\\"endpoint-1472821604763\\",\\"drivingInput\\":true,\\"longName\\":\\"Table.endpoint-1472821604763\\",\\"displayName\\":\\"in1\\",\\"acceptedTypes\\":[\\"Object\\"]},{\\"canConnect\\":true,\\"export\\":false,\\"connected\\":false,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"id\\":\\"myId_7_1472821608592\\",\\"variadic\\":{\\"isLast\\":true,\\"index\\":2},\\"name\\":\\"endpoint1472821608591\\",\\"drivingInput\\":true,\\"displayName\\":\\"in2\\",\\"acceptedTypes\\":[\\"Object\\"],\\"longName\\":\\"Table.endpoint1472821608591\\"}],\\"canClearState\\":true,\\"layout\\":{\\"position\\":{\\"left\\":\\"242px\\",\\"top\\":\\"255px\\"}},\\"name\\":\\"Table\\",\\"outputs\\":[],\\"options\\":{\\"uiResendLast\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"maxRows\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"uiResendAll\\":{\\"value\\":false,\\"type\\":\\"boolean\\"}},\\"jsModule\\":\\"TableModule\\"}],\\"settings\\":{\\"editorState\\":{\\"runTab\\":\\"#tab-historical\\"},\\"speed\\":\\"0\\",\\"timeOfDayFilter\\":{\\"timeOfDayStart\\":\\"00:00:00\\",\\"timeOfDayEnd\\":\\"23:59:00\\"},\\"endDate\\":\\"2016-08-29\\",\\"beginDate\\":\\"2016-08-29\\"},\\"hasExports\\":true,\\"uiChannel\\":{\\"id\\":\\"4AOpGMe6SYecTgPrsDR4pQ\\",\\"webcomponent\\":null,\\"name\\":\\"Notifications\\"}}', '2016-09-02 13:06:55', 'SubCanvasSpec-sub', NULL, NULL, NULL, NULL, NULL, 'stopped', 1);
		""")
		sql("""
INSERT INTO `canvas` (`id`, `version`, `adhoc`, `date_created`, `example`, `has_exports`, `json`, `last_updated`, `name`, `request_url`, `runner`, `serialization_time`, `serialized`, `server`, `state`, `user_id`)
VALUES
\t('eyescpGFRiKzr9WxU2k0Yw', 165, 00000000, '2016-08-29 13:50:12', 00000000, 00000001, '{\\"name\\":\\"SubCanvasSpec-top\\",\\"modules\\":[{\\"hash\\":1,\\"textFieldValue\\":\\"\\",\\"uiChannel\\":{\\"id\\":\\"aBH9ZPAxTE2XvgJ7bKgQXg\\",\\"webcomponent\\":\\"streamr-text-field\\",\\"name\\":\\"TextField\\"},\\"params\\":[],\\"type\\":\\"module\\",\\"id\\":220,\\"textFieldHeight\\":76,\\"inputs\\":[],\\"textFieldWidth\\":174,\\"canClearState\\":true,\\"layout\\":{\\"position\\":{\\"left\\":\\"103px\\",\\"top\\":\\"118px\\"}},\\"name\\":\\"TextField\\",\\"outputs\\":[{\\"id\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"canConnect\\":true,\\"canBeNoRepeat\\":true,\\"name\\":\\"out\\",\\"connected\\":true,\\"longName\\":\\"TextField.out\\",\\"noRepeat\\":true,\\"type\\":\\"String\\",\\"targets\\":[\\"tfG-zZItTDmS4G7CdTzp7A\\",\\"32yzaprIS7S64pfbQOQFCg\\",\\"69oKfPEuQwayNbAuXv8TSw\\",\\"c24ESIiATW67SYdlSovPTw\\",\\"XwspREeeTAWeGL7HBKtzKQ\\",\\"dY7EH1ydQGaLoTemJnJrog\\",\\"fXZ1hpyRRFqy5MGLv8helg\\",\\"OF_aBTeNS76gR9hd_8sHGQ\\",\\"K5moEsdqSzyhMaeYnb5xNQ\\"]}],\\"widget\\":\\"StreamrTextField\\",\\"options\\":{\\"uiResendLast\\":{\\"value\\":1,\\"type\\":\\"int\\"},\\"uiResendAll\\":{\\"value\\":false,\\"type\\":\\"boolean\\"}},\\"jsModule\\":\\"InputModule\\"},{\\"id\\":223,\\"canRefresh\\":true,\\"inputs\\":[{\\"canConnect\\":true,\\"connected\\":true,\\"canHaveInitialValue\\":true,\\"type\\":\\"String\\",\\"requiresConnection\\":true,\\"canToggleDrivingInput\\":true,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"feedback\\":false,\\"id\\":\\"32yzaprIS7S64pfbQOQFCg\\",\\"canBeFeedback\\":true,\\"drivingInput\\":true,\\"initialValue\\":null,\\"name\\":\\"key\\",\\"longName\\":\\"ForEach.key\\",\\"value\\":\\"\\\\u00E4l\\\\u00E4l\\\\u00E4l\\\\u00E4l\\\\u00E4\\",\\"acceptedTypes\\":[\\"String\\"]},{\\"canConnect\\":true,\\"export\\":true,\\"connected\\":true,\\"type\\":\\"Object\\",\\"requiresConnection\\":true,\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"id\\":\\"69oKfPEuQwayNbAuXv8TSw\\",\\"name\\":\\"label\\",\\"drivingInput\\":true,\\"value\\":\\"\\\\u00E4l\\\\u00E4l\\\\u00E4l\\\\u00E4l\\\\u00E4\\",\\"longName\\":\\"ForEach.label\\",\\"acceptedTypes\\":[\\"Object\\"]},{\\"canConnect\\":true,\\"export\\":true,\\"connected\\":true,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"id\\":\\"OF_aBTeNS76gR9hd_8sHGQ\\",\\"variadic\\":{\\"isLast\\":false,\\"index\\":1},\\"name\\":\\"endpoint-1472821604763\\",\\"drivingInput\\":true,\\"longName\\":\\"ForEach.endpoint-1472821604763\\",\\"displayName\\":\\"in1\\",\\"acceptedTypes\\":[\\"Object\\"]}],\\"hash\\":4,\\"canClearState\\":true,\\"name\\":\\"ForEach\\",\\"layout\\":{\\"position\\":{\\"left\\":\\"612px\\",\\"top\\":\\"82px\\"}},\\"params\\":[{\\"canConnect\\":true,\\"updateOnChange\\":true,\\"possibleValues\\":[{\\"value\\":\\"eyescpGFRiKzr9WxU2k0Yw\\",\\"name\\":\\"SubCanvasSpec-top\\"},{\\"value\\":\\"VWo3BDECTASlAdtZk7QeeQ\\",\\"name\\":\\"SubCanvasSpec-sub\\"}],\\"connected\\":false,\\"type\\":\\"Canvas\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":true,\\"id\\":\\"_iIuDBrCSoqhuX_2Vf8_og\\",\\"drivingInput\\":false,\\"name\\":\\"canvas\\",\\"value\\":\\"VWo3BDECTASlAdtZk7QeeQ\\",\\"longName\\":\\"ForEach.canvas\\",\\"defaultValue\\":\\"VWo3BDECTASlAdtZk7QeeQ\\",\\"acceptedTypes\\":[\\"Canvas\\"]}],\\"canvasesByKey\\":{},\\"outputs\\":[{\\"canBeNoRepeat\\":true,\\"canConnect\\":true,\\"id\\":\\"mJs0o416T7ukb4jpyTs_Aw\\",\\"connected\\":false,\\"name\\":\\"key\\",\\"longName\\":\\"ForEach.key\\",\\"noRepeat\\":true,\\"type\\":\\"String\\"},{\\"canConnect\\":true,\\"id\\":\\"-iIT8BZ_T6ONKXmlRVIwxQ\\",\\"connected\\":false,\\"name\\":\\"map\\",\\"longName\\":\\"ForEach.map\\",\\"type\\":\\"Map\\"}],\\"type\\":\\"module\\",\\"jsModule\\":\\"ForEachModule\\"},{\\"hash\\":5,\\"tableConfig\\":{\\"headers\\":[\\"timestamp\\",\\"out\\"]},\\"uiChannel\\":{\\"id\\":\\"cny8lZ28TvKtZ-SLPs32VQ\\",\\"webcomponent\\":\\"streamr-table\\",\\"name\\":\\"Table\\"},\\"params\\":[],\\"type\\":\\"module event-table-module\\",\\"id\\":527,\\"inputs\\":[{\\"canConnect\\":true,\\"connected\\":true,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"id\\":\\"XwspREeeTAWeGL7HBKtzKQ\\",\\"variadic\\":{\\"isLast\\":false,\\"index\\":1},\\"name\\":\\"endpoint-1472545463031\\",\\"drivingInput\\":true,\\"longName\\":\\"Table.endpoint-1472545463031\\",\\"displayName\\":\\"in1\\",\\"acceptedTypes\\":[\\"Object\\"]},{\\"canConnect\\":true,\\"connected\\":false,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"id\\":\\"myId_5_1472545465692\\",\\"drivingInput\\":true,\\"name\\":\\"endpoint1472545465692\\",\\"variadic\\":{\\"isLast\\":true,\\"index\\":2},\\"longName\\":\\"Table.endpoint1472545465692\\",\\"displayName\\":\\"in2\\",\\"acceptedTypes\\":[\\"Object\\"]}],\\"canClearState\\":true,\\"layout\\":{\\"position\\":{\\"left\\":\\"89px\\",\\"top\\":\\"344px\\"}},\\"name\\":\\"Table\\",\\"outputs\\":[],\\"jsModule\\":\\"TableModule\\",\\"options\\":{\\"uiResendLast\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"maxRows\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"uiResendAll\\":{\\"value\\":false,\\"type\\":\\"boolean\\"}}},{\\"hash\\":7,\\"uiChannel\\":{\\"id\\":\\"odob-JGMSz65Po9hWFaPfQ\\",\\"webcomponent\\":null,\\"name\\":\\"Notifications\\"},\\"params\\":[{\\"canConnect\\":true,\\"updateOnChange\\":true,\\"possibleValues\\":[{\\"value\\":\\"eyescpGFRiKzr9WxU2k0Yw\\",\\"name\\":\\"SubCanvasSpec-top\\"},{\\"value\\":\\"VWo3BDECTASlAdtZk7QeeQ\\",\\"name\\":\\"SubCanvasSpec-sub\\"}],\\"connected\\":false,\\"type\\":\\"Canvas\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":true,\\"id\\":\\"ep_UVwTVZN7STOJwIsLSsbhQQ\\",\\"drivingInput\\":false,\\"name\\":\\"canvas\\",\\"value\\":\\"VWo3BDECTASlAdtZk7QeeQ\\",\\"longName\\":\\"SubCanvasSpec-sub.canvas\\",\\"defaultValue\\":\\"VWo3BDECTASlAdtZk7QeeQ\\",\\"acceptedTypes\\":[\\"Canvas\\"]}],\\"type\\":\\"module\\",\\"id\\":81,\\"canRefresh\\":true,\\"inputs\\":[{\\"canConnect\\":true,\\"id\\":\\"fXZ1hpyRRFqy5MGLv8helg\\",\\"export\\":true,\\"drivingInput\\":true,\\"connected\\":true,\\"name\\":\\"label\\",\\"longName\\":\\"Label.label\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":true,\\"acceptedTypes\\":[\\"Object\\"],\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\"},{\\"canConnect\\":true,\\"export\\":true,\\"connected\\":true,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"id\\":\\"K5moEsdqSzyhMaeYnb5xNQ\\",\\"variadic\\":{\\"isLast\\":false,\\"index\\":1},\\"name\\":\\"endpoint-1472821604763\\",\\"drivingInput\\":true,\\"longName\\":\\"Table.endpoint-1472821604763\\",\\"displayName\\":\\"in1\\",\\"acceptedTypes\\":[\\"Object\\"]}],\\"canClearState\\":true,\\"layout\\":{\\"position\\":{\\"left\\":\\"618px\\",\\"top\\":\\"301px\\"}},\\"name\\":\\"SubCanvasSpec-sub\\",\\"modules\\":[{\\"id\\":145,\\"inputs\\":[{\\"canConnect\\":true,\\"id\\":\\"fXZ1hpyRRFqy5MGLv8helg\\",\\"export\\":true,\\"drivingInput\\":true,\\"connected\\":true,\\"name\\":\\"label\\",\\"longName\\":\\"Label.label\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":true,\\"acceptedTypes\\":[\\"Object\\"],\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\"}],\\"hash\\":5,\\"canClearState\\":false,\\"layout\\":{\\"position\\":{\\"left\\":\\"189px\\",\\"top\\":\\"94px\\"}},\\"name\\":\\"Label\\",\\"params\\":[],\\"uiChannel\\":{\\"id\\":\\"sLCW96ZzQQCCILQHwu35og\\",\\"webcomponent\\":\\"streamr-label\\",\\"name\\":\\"Label (TextField.out)\\"},\\"type\\":\\"module dashboard\\",\\"outputs\\":[],\\"jsModule\\":\\"LabelModule\\"},{\\"hash\\":7,\\"tableConfig\\":{\\"headers\\":[\\"timestamp\\",\\"out\\"]},\\"uiChannel\\":{\\"id\\":\\"_w0q24z1S-ebnfWbEmdQog\\",\\"webcomponent\\":\\"streamr-table\\",\\"name\\":\\"Table\\"},\\"params\\":[],\\"type\\":\\"module event-table-module\\",\\"id\\":527,\\"inputs\\":[{\\"canConnect\\":true,\\"export\\":true,\\"connected\\":true,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"sourceId\\":\\"3GAu-zgYTGOVpspmgIZBwQ\\",\\"id\\":\\"K5moEsdqSzyhMaeYnb5xNQ\\",\\"variadic\\":{\\"isLast\\":false,\\"index\\":1},\\"name\\":\\"endpoint-1472821604763\\",\\"drivingInput\\":true,\\"longName\\":\\"Table.endpoint-1472821604763\\",\\"displayName\\":\\"in1\\",\\"acceptedTypes\\":[\\"Object\\"]},{\\"canConnect\\":true,\\"export\\":false,\\"connected\\":false,\\"jsClass\\":\\"VariadicInput\\",\\"type\\":\\"Object\\",\\"requiresConnection\\":false,\\"canToggleDrivingInput\\":false,\\"id\\":\\"myId_7_1472821608592\\",\\"variadic\\":{\\"isLast\\":true,\\"index\\":2},\\"name\\":\\"endpoint1472821608591\\",\\"drivingInput\\":true,\\"longName\\":\\"Table.endpoint1472821608591\\",\\"displayName\\":\\"in2\\",\\"acceptedTypes\\":[\\"Object\\"]}],\\"canClearState\\":true,\\"layout\\":{\\"position\\":{\\"left\\":\\"242px\\",\\"top\\":\\"255px\\"}},\\"name\\":\\"Table\\",\\"outputs\\":[],\\"jsModule\\":\\"TableModule\\",\\"options\\":{\\"uiResendLast\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"maxRows\\":{\\"value\\":20,\\"type\\":\\"int\\"},\\"uiResendAll\\":{\\"value\\":false,\\"type\\":\\"boolean\\"}}}],\\"outputs\\":[],\\"jsModule\\":\\"CanvasModule\\",\\"options\\":{\\"uiResendLast\\":{\\"value\\":0,\\"type\\":\\"int\\"},\\"uiResendAll\\":{\\"value\\":true,\\"type\\":\\"boolean\\"}}}],\\"settings\\":{\\"editorState\\":{\\"runTab\\":\\"#tab-realtime\\"},\\"speed\\":\\"0\\",\\"timeOfDayFilter\\":{\\"timeOfDayStart\\":\\"00:00:00\\",\\"timeOfDayEnd\\":\\"23:59:00\\"},\\"endDate\\":\\"2016-08-29\\",\\"beginDate\\":\\"2016-08-29\\"},\\"hasExports\\":true,\\"uiChannel\\":{\\"id\\":\\"V_GXmKCXTtOxMICLYhq4bg\\",\\"webcomponent\\":null,\\"name\\":\\"Notifications\\"}}', '2016-09-02 13:08:18', 'SubCanvasSpec-top', 'http://192.168.10.137:8081/unifina-core/api/v1/canvases/eyescpGFRiKzr9WxU2k0Yw', 's-1472821655728', NULL, NULL, '192.168.10.137', 'stopped', 1);
		""")
	}
}
