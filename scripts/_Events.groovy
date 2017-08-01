
import grails.util.Environment

eventConfigureTomcat = { tomcat ->
	
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

eventPackagingEnd = { args ->
	boolean prod = Environment.getCurrent() == Environment.PRODUCTION
	println "Running webpack build in ${prod ? "production" : "development"} mode"
	Runtime runtime = Runtime.getRuntime()
	String command = prod ? "npm run build" : "npm run build-dev"

	Process process = runtime.exec(command)
	StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
	outputGobbler.start()

	// Wait for npm to exit before continuing
	int exitValue = process.waitFor()
	if (exitValue > 0) {
		throw new RuntimeException("Webpack failed!")
	}
}

// https://stackoverflow.com/questions/1732455/redirect-process-output-to-stdout
class StreamGobbler extends Thread {
	InputStream is

	// reads everything from is until empty.
	StreamGobbler(InputStream is) {
		this.is = is
	}

	void run() {
		try {
			InputStreamReader isr = new InputStreamReader (is)
			BufferedReader br = new BufferedReader (isr)
			String line = null
			while ( (line = br.readLine()) != null) {
				System.out.println(line)
			}
			this.interrupt()
		} catch (IOException ioe) {
			ioe.printStackTrace()
			this.interrupt()
		}
	}
}

