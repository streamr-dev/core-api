import grails.util.Environment

import java.nio.file.Paths
import java.util.regex.Pattern

eventTestPhaseStart = { args ->
	println "eventTestPhaseStart called: $args"
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

eventCreateWarStart = {warname, stagingDir ->
	if (Environment.getCurrent() == Environment.PRODUCTION) {
		event("AddWebpackFilenamesStart", [warname, stagingDir])

		// Filename patterns include the chunk hash in prod
		Pattern jsPattern = ~/.*\.bundle\.[0-9a-f]*\.js/
		Pattern cssPattern = ~/.*\.bundle\.[0-9a-f]*\.css/

		List jsFiles = []
		List cssFiles = []

		File webpackBundlesDir = Paths.get(Ant.antProject.properties['base.dir'], 'web-app', 'webpack-bundles').toFile()
		if (!webpackBundlesDir.exists()) {
			throw new FileNotFoundException("Can't find /web-app/webpack-bundles directory!")
		}

		webpackBundlesDir.eachFile { File file ->
			if (file.getName().matches(jsPattern)) {
				jsFiles << file.getName()
			} else if (file.getName().matches(cssPattern)) {
				cssFiles << file.getName()
			}
		}

		if (jsFiles.isEmpty()) {
			throw new FileNotFoundException("Couldn't find webpack js bundles matching regex: ${jsPattern.pattern()} in ${webpackBundlesDir.getAbsolutePath()}")
		}
		if (cssFiles.isEmpty()) {
			throw new FileNotFoundException("Couldn't find webpack css bundles matching regex: ${cssPattern.pattern()} in ${webpackBundlesDir.getAbsolutePath()}")
		}

		println "Found webpack js files: ${jsFiles}"
		println "Found webpack css files: ${cssFiles}"

		String applicationPropertiesFilename = "${stagingDir}/WEB-INF/classes/application.properties"
		println "Adding webpack filenames to ${applicationPropertiesFilename}"

		writeProperties([
				'webpack.jsFiles': jsFiles.join(','),
				'webpack.cssFiles': cssFiles.join(',')
		], applicationPropertiesFilename)

		event("AddWebpackFilenamesEnd", [warname, stagingDir])
	}
}

private void writeProperties(Map properties, String propertyFile) {
	Ant.propertyfile(file: propertyFile) {
		properties.each { k,v->
			entry(key: k, value: v)
		}
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