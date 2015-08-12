eventConfigureTomcat = {tomcat ->
	
	// Enable compression, from http://www.slideshare.net/gr8conf/gr8conf-2011-tuning-grails-applications-by-peter-ledbrook
	tomcat.connector.setAttribute("compression","on")
//	tomcat.connector.setAttribute("compressionMinSize","10240")
	tomcat.connector.setAttribute("compressableMimeType","application/json,text/html")
	tomcat.connector.port = serverPort
	
	// The below stuff is from an example at http://roshandawrani.wordpress.com/2011/03/13/grails-tip-configuring-embedded-tomcat-instance-used-in-developmenttest-env/
	
//	def connector = new Connector("org.apache.coyote.http11.Http11NioProtocol")
//	connector.port = System.getProperty("server.port", "8080").toInteger()
//	connector.redirectPort = "8443"
//	connector.protocol = "HTTP/1.1"
//	connector.connectionTimeout = "20000"
//
//	tomcat.connector = connector
//	tomcat.service.addConnector connector
}

eventTestPhaseStart = { args ->
	println "eventTestPhaseStart called in unifina-core: $args"
	System.properties["grails.test.phase"] = args
}