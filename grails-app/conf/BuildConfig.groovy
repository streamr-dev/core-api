import grails.util.Environment


def env = System.getenv()

def sutHost = env['SUT_HOST'] ?: 'localhost'
def sutPort = env['SUT_PORT'] ?: '8081'

grails.server.port.http = sutPort

rails.servlet.version = "3.0"
grails.tomcat.nio = true

grails.project.target.level = 1.7
grails.project.source.level = 1.7

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.war.file = "target/ROOT.war"

def gebVersion = "0.9.3"
def seleniumVersion = "2.48.2"

grails.project.dependency.resolver = "maven" // or ivy

// grails.project.fork.run = false
//grails.project.fork = [
//	run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
//	test: false
//]
grails.project.fork = [
    run: [maxMemory: System.getProperty("maxMemory") ? Integer.parseInt(System.getProperty("maxMemory")) : 4196, minMemory: 256, debug: false, maxPerm: 512, forkReserve:false],
    test: [maxMemory: System.getProperty("maxMemory") ? Integer.parseInt(System.getProperty("maxMemory")) : 4196, minMemory: 256, debug: false, maxPerm: 512, forkReserve:false, daemon:true, jvmArgs: ["-Dwebdriver.chrome.driver="+env["CHROMEDRIVER"]]]
]

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    //    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
    repositories {
        // Fast local repos first
        grailsHome()
        mavenLocal()

		// Maven central
		mavenRepo "http://repo1.maven.org/maven2/"

		// Ethereum Repository
		mavenRepo "https://dl.bintray.com/ethereum/maven/"

        // Remote Grails repos
        grailsPlugins()
        grailsCentral()
        // New Grails repo
        mavenRepo "https://repo.grails.org/grails/plugins"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        compile('log4j:log4j:1.2.16')

        runtime('commons-net:commons-net:3.3')
        runtime('org.apache.commons:commons-math3:3.2')
        runtime('commons-codec:commons-codec:1.6')
        runtime('com.opencsv:opencsv:3.3')
        runtime('de.ruedigermoeller:fst:2.43')
		
        compile('org.atmosphere:atmosphere-runtime:1.0.0.beta5')
		runtime('joda-time:joda-time:2.9.3')
		compile('com.udojava:EvalEx:1.6')

		compile('org.apache.kafka:kafka-clients:0.9.0.1')
        compile('com.mashape.unirest:unirest-java:1.3.3')

		compile group: 'org.eclipse.paho', name: 'org.eclipse.paho.client.mqttv3', version: '1.1.1'

		// http://www.stringtemplate.org/
		// http://mvnrepository.com/artifact/org.antlr/ST4
		compile group: 'org.antlr', name: 'ST4', version: '4.0.8'

        runtime('com.amazonaws:aws-java-sdk:1.7.5')

		runtime('joda-time:joda-time:2.9.3')

        test "org.seleniumhq.selenium:selenium-firefox-driver:$seleniumVersion"
        test "org.seleniumhq.selenium:selenium-chrome-driver:$seleniumVersion"
        test "org.seleniumhq.selenium:selenium-support:$seleniumVersion"
        test "org.gebish:geb-spock:$gebVersion"

		runtime('org.twitter4j:twitter4j-core:4.0.4')
		runtime('org.twitter4j:twitter4j-stream:4.0.4')
		runtime('com.twitter:hbc-core:2.2.0') {
			excludes 'com.google.guava:guava:14.0.1'
		}

		runtime 'mysql:mysql-connector-java:5.1.20'
		compile 'org.postgresql:postgresql:9.4.1208.jre7'
		compile 'org.mongodb:mongodb-driver:3.2.1'
		compile('biz.paluch.redis:lettuce:3.5.0.Final')
		compile('com.datastax.cassandra:cassandra-driver-core:3.1.0')
		compile('org.ethereum:ethereumj-core:1.4.3-RELEASE') {
			excludes 'ch.qos.logback:logback-classic:*'
			excludes 'org.springframework:spring-core:*'
			excludes 'org.springframework:spring-context:*'
			excludes 'org.springframework:spring-orm:*'
		}
    }

    plugins {
        // plugins for the build system only
		
        build(":tomcat:7.0.42",
			 ":release:3.0.1"//,
            /*":rest-client-builder:2.0.1"*/) {
            export = false
        }
        
        compile ":mail:1.0.7"

	    // plugins needed at runtime but not for compilation
		runtime ':hibernate:3.6.10.19'
			  
		// Required by cached-resources but transitive dependency declaration is missing	  
		compile ":cache-headers:1.1.7"
		runtime ':database-migration:1.4.0'
        runtime ":spring-security-core:2.0-RC4"
        runtime ":resources:1.2.14"
        runtime ":cached-resources:1.0"
        runtime ":zipped-resources:1.0"
        compile ":uglify-js-minified-resources:0.1.1"

        runtime ":cors:1.1.8"
		
        test ":plastic-criteria:1.5"
        test ":geb:$gebVersion"
    }
}
