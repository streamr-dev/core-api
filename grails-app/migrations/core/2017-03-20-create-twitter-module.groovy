package core
databaseChangeLog = {

	changeSet(author: "aapeli", id: "create-twitter-module") {
		sql("""
			INSERT INTO module (
				id, version, category_id, implementing_class, name, js_module, hide, type, module_package_id, json_help, alternative_names, webcomponent
			) VALUES (
				159, 1, 25, 'com.unifina.signalpath.twitter.TwitterModule', 'Twitter', 'GenericModule', 1, 'module', 1, '{\\"params\\":{\\"stream\\":\\"Selected Twitter stream\\"},\\"paramNames\\":[\\"stream\\"],\\"inputs\\":{},\\"inputNames\\":[],\\"outputs\\":{\\"tweet\\":\\"Tweet text\\",\\"username\\":\\"Screen name of the user\\",\\"name\\":\\"Full name of the user\\",\\"language\\":\\"Language code\\",\\"followers\\":\\"Number of followers\\",\\"retweet?\\":\\"1 if this is a retweet, 0 otherwise\\",\\"reply?\\":\\"1 if this is a reply, 0 otherwise\\"},\\"outputNames\\":[\\"tweet\\",\\"username\\",\\"name\\",\\"language\\",\\"followers\\",\\"retweet?\\",\\"reply?\\"],\\"helpText\\":\\"This is a source module for tweets. Twitter streams are tweets that match a group of keywords that define the stream.\\"}', NULL, NULL);
		""")
	}
}
