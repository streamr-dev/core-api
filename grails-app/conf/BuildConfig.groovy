import grails.util.Environment

grails.servlet.version = "3.0"
grails.tomcat.nio = true

grails.project.target.level = 1.7
grails.project.source.level = 1.7

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

def gebVersion = "0.9.3"
def seleniumVersion = "2.41.0"

//grails.project.dependency.resolver = "maven" // or ivy

grails.project.fork = [
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false]
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
		
		// Unifina Nexus server
		mavenRepo "http://192.168.10.21:8081/content/repositories/central/"
		mavenRepo "http://192.168.10.21:8081/content/repositories/releases/"
		mavenRepo "http://192.168.10.21:8081/content/repositories/snapshots/"
		
		// Remote Grails repos
        grailsPlugins()
        grailsCentral()
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.21'
		compile('log4j:log4j:1.2.16')
		
		runtime('commons-net:commons-net:3.3')
		runtime('org.apache.commons:commons-math3:3.2')
		compile('org.atmosphere:atmosphere-runtime:1.0.0.beta5')
		
		compile('org.apache.kafka:kafka_2.8.2:0.8.0') {
			excludes "slf4j-simple"
		}
		compile('com.unifina:kafka-client:0.0.6') {
			excludes "slf4j-simple"
		}
		
		compile('com.mashape.unirest:unirest-java:1.3.3')
		
		runtime('com.amazonaws:aws-java-sdk:1.7.5')

		test "org.seleniumhq.selenium:selenium-firefox-driver:$seleniumVersion"
		test "org.seleniumhq.selenium:selenium-chrome-driver:$seleniumVersion"
		test "org.gebish:geb-spock:$gebVersion"
    }

    plugins {
		// plugins for the build system only
		
        build(":tomcat:7.0.42",
			 ":release:3.0.1"//,
              /*":rest-client-builder:2.0.1"*/) {
            export = false
        }

	    // plugins needed at runtime but not for compilation
		runtime ':hibernate:3.6.10.2'
			  
		// Required by cached-resources but transitive dependency declaration is missing	  
		compile ":cache-headers:1.1.5"
			  
		runtime ":spring-security-core:1.2.7"
		runtime ":jquery:1.8.3"
		runtime ":jquery-ui:1.8.24"
		runtime ":resources:1.2.8"
		runtime ":cached-resources:1.0"
		runtime ":zipped-resources:1.0"
		build ':yui-war-minify:1.5'

		test ":geb:$gebVersion"
    }
}
