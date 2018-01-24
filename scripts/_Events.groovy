import grails.util.Environment

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

