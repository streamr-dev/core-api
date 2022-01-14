grails.server.port.http = '8081'
grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.tomcat.nio = true

grails.project.target.level = 1.8
grails.project.source.level = 1.8
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.war.file = "target/ROOT.war" // "target/${appName}-${appVersion}.war"

grails.project.fork = [
	run: [
		maxMemory: System.getProperty("maxMemory") ? Integer.parseInt(System.getProperty("maxMemory")) : 4196,
		minMemory: 256,
		debug: false,
		maxPerm: 512,
		forkReserve: false,
		jvmArgs: [
			"-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true"
		]
	],
	test: [
		maxMemory: System.getProperty("maxMemory") ? Integer.parseInt(System.getProperty("maxMemory")) : 4196,
		minMemory: 256,
		debug: false,
		maxPerm: 512,
		forkReserve: false,
		daemon: true,
		jvmArgs: [
			"-Djava.awt.headless=true",
			"-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true"
		]
	]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {

	// inherit Grails' default dependencies
	inherits("global") {
		// specify dependency exclusions here; for example, uncomment this to disable ehcache:
		// excludes 'ehcache'
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	checksums true // Whether to verify checksums on resolve
	legacyResolve false
	// whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

	repositories {
		mavenRepo "https://repo.grails.org/grails/core"
		mavenRepo "https://repo.grails.org/grails/plugins"
		mavenRepo "https://repo1.maven.org/maven2/"
		mavenLocal()
	}

	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
		// https://stackoverflow.com/questions/8751508/grails-buildconfig-groovy-difference-between-build-compile-and-runtime

		compile('log4j:log4j:1.2.16')
		compile('biz.paluch.redis:lettuce:3.5.0.Final')
		compile('com.google.code.findbugs:jsr305:3.0.2')
		compile('org.jetbrains:annotations:17.0.0')
		compile('org.springframework.security:spring-security-web:4.2.9.RELEASE') { // Needed for CORS plugin
			excludes('org.springframework:spring-web:*')
		}
		compile('org.web3j:core:4.8.9') {
			excludes "org.java-websocket:Java-WebSocket:*" // Version conflict with com.streamr:client
		}
		compile('com.amazonaws:aws-java-sdk-s3:1.12.10')
		compile('org.imgscalr:imgscalr-lib:4.2')
		compile('commons-io:commons-io:2.4')
		compile('com.streamr:client:2.3.0') {
			excludes "org.web3j:codegen:*"
		}

		compile('com.google.code.gson:gson:2.8.5')
		runtime('mysql:mysql-connector-java:5.1.20')
		runtime('com.mchange:c3p0:0.9.5.5')
		runtime('commons-codec:commons-codec:1.15')

		test('cglib:cglib:3.2.6')
	}

	plugins {
		provided(":tomcat:8.0.50") {
			export = false
		}

		compile(":mail:1.0.8-SNAPSHOT")

		runtime(':hibernate4:4.3.10')
		runtime(":cors:1.3.0") {
			excludes('spring-security-core')
			excludes('spring-security-web')
		}
		runtime(':database-migration:1.4.2-SNAPSHOT')

		test(":plastic-criteria:1.6.7")
	}
}
