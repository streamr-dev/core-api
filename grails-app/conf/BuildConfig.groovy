import grails.util.Environment

grails.project.target.level = 1.7
grails.project.source.level = 1.7

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
    repositories {
        grailsCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.21'
		compile('log4j:log4j:1.2.16')
		compile('org.apache.commons:commons-math:2.2')
		compile('org.atmosphere:atmosphere-runtime:1.0.0.beta5')
    }

    plugins {
        build(":tomcat:$grailsVersion",
              ":release:2.2.0",
              ":rest-client-builder:1.0.3") {
            export = false
        }
		
		// Required by cached-resources but transitive dependency declaration is missing	  
		compile ":cache-headers:1.1.5"
			  
		runtime ":hibernate:$grailsVersion"
			  
		runtime ":spring-security-core:1.2.7"
		runtime ":jquery:1.8.3"
		runtime ":jquery-ui:1.8.24"
		runtime ":resources:1.1.6"
		runtime ":cached-resources:1.0"
		runtime ":zipped-resources:1.0"
    }
}
