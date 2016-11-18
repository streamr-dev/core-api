package core
databaseChangeLog = {

	changeSet(author: "eric", id: "random-modules") {

		def modules = [
			[id: 562, categoryId: 54, name: "RandomNumber", jsonHelp: '{"params":{"min":"lower bound of interval to sample from","max":"upper bound of interval to sample from"},"paramNames":["min","max"],"inputs":{"trigger":"when value is received, activates module"},"inputNames":["trigger"],"outputs":{"out":"the random number"},"outputNames":["out"],"helpText":"<p>Generate random numbers between [<em>min</em>, <em>max</em>] with uniform probability.</p>"}', ],
			[id: 563, categoryId: 54, name: "RandomNumberGaussian", jsonHelp: '{"params":{"mean":"mean of normal distribution","sd":"standard deviation of normal distribution"},"paramNames":["mean","sd"],"inputs":{"trigger":"when value is received, activates module"},"inputNames":["trigger"],"outputs":{"out":"the random number"},"outputNames":["out"],"helpText":"<p>Generate random numbers from normal (Gaussian) distribution with mean&nbsp;<em>mean</em>&nbsp;and standard deviation&nbsp;<em>sd</em>.</p>"}', ],
			[id: 564, categoryId: 27, name: "RandomString", jsonHelp: '{"params":{"length":"length of strings to generate"},"paramNames":["length"],"inputs":{"trigger":"when value is received, activates module"},"inputNames":["trigger"],"outputs":{"out":"the random string"},"outputNames":["out"],"helpText":"<p>Generate fixed-length random strings from an equiprobable symbol pool. Allowed symbols can be configured from module settings.</p>"}', ],
			[id: 565, categoryId: 52, name: "ShuffleList", jsonHelp: '{"params":{},"paramNames":[],"inputs":{"in":"input list"},"inputNames":["in"],"outputs":{"out":"input list randomly ordered"},"outputNames":["out"],"helpText":"<p>Shuffle the items of a list.</p>"}', ],
		]

		insert(tableName: "module_category") {
			column(name: "id", valueNumeric: 54)
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Random")
			column(name: "sort_order", valueNumeric: 142)
			column(name: "parent_id", valueNumeric: 15) // Time Series
			column(name: "module_package_id", valueNumeric: 1)
		}

		modules.eachWithIndex { module, i ->
			insert(tableName: "module") {
				column(name: "id", valueNumeric: module.id)
				column(name: "version", valueNumeric: 0)
				column(name: "category_id", valueNumeric: module.categoryId)
				column(name: "implementing_class", value: "com.unifina.signalpath.random.${module.name}")
				column(name: "name", value: module.name)
				column(name: "js_module", value: "GenericModule")
				column(name: "type", value: "module")
				column(name: "module_package_id", valueNumeric: 1)
				column(name: "json_help", value: module.jsonHelp)
			}
		}
	}
}
