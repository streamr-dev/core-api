/*****
 * This config file gets merged with the application config file.
 * The application config file can override anything defined here.
 * 
 * Stuff you'll want to configure in the application Config.groovy:
 * 
 * - grails.serverURL in production
 * - AWS credentials
 * - email sending config
 */

 /**
  * Grails configuration 
  */

 // The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
 grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
 grails.mime.types = [ // the first one is the default format
	 all:           '*/*', // 'all' maps to '*' or the first available format in withFormat
	 atom:          'application/atom+xml',
	 css:           'text/css',
	 csv:           'text/csv',
	 form:          'application/x-www-form-urlencoded',
	 html:          ['text/html','application/xhtml+xml'],
	 js:            'text/javascript',
	 json:          ['application/json', 'text/json'],
	 multipartForm: 'multipart/form-data',
	 rss:           'application/rss+xml',
	 text:          'text/plain',
	 hal:           ['application/hal+json','application/hal+xml'],
	 xml:           ['text/xml', 'application/xml']
 ]
 
 // URL Mapping Cache Max Size, defaults to 5000
 //grails.urlmapping.cache.maxsize = 1000
 
 // What URL patterns should be processed by the resources plugin
 grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*', "/js/polymer/*", "/js/tours/*"]
 grails.resources.adhoc.includes = ['/images/**', '/css/**', '/js/**', '/plugins/**']
 
 grails.resources.processing.enabled = true
 
 environments {
	 development {
		 grails.resources.mappers.bundle.excludes = ['**/*.*']
		 grails.resources.mappers.hashandcache.excludes = ['**/*.*']
		 grails.resources.mappers.zip.excludes = ['**/*.*']
		 grails.resources.processing.excludes = ['**/*.js']
		 grails.resources.mappers.uglifyjs.excludes = ['**/*.*']
	 }
	 test {
		 grails.resources.mappers.bundle.excludes = ['**/*.*']
		 grails.resources.mappers.hashandcache.excludes = ['**/*.*']
		 grails.resources.mappers.zip.excludes = ['**/*.*']
		 grails.resources.processing.excludes = ['**/*.js']
		 grails.resources.mappers.uglifyjs.excludes = ['**/*.*']
	 }
 }
 
 environments {
	 test {
		 grails.reload.enabled = true
		 disable.auto.recompile = false // Recompilation of Java Sources
		 grails.gsp.enable.reload = true // Recompilation of GSPs
	 }
 }
 
 // Legacy setting for codec used to encode data with ${}
 grails.views.default.codec = "html"
 
 // The default scope for controllers. May be prototype, session or singleton.
 // If unspecified, controllers are prototype scoped.
 grails.controllers.defaultScope = 'singleton'
 
 // GSP settings
 grails {
	 views {
		 gsp {
			 encoding = 'UTF-8'
			 htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
			 codecs {
				 expression = 'html' // escapes values inside ${}
				 scriptlet = 'html' // escapes output from scriptlets in GSPs
				 taglib = 'none' // escapes output from taglibs
				 staticparts = 'none' // escapes output from static template parts
			 }
		 }
		 // escapes all not-encoded output at final stage of outputting
		 // filteringCodecForContentType.'text/html' = 'html'
	 }
 }
 
 
 grails.converters.encoding = "UTF-8"
 // scaffolding templates configuration
 grails.scaffolding.templates.domainSuffix = 'Instance'
 
 // Set to false to use the new Grails 1.2 JSONBuilder in the render method
 grails.json.legacy.builder = false
 // enabled native2ascii conversion of i18n properties files
 grails.enable.native2ascii = true
 // packages to include in Spring bean scanning
 grails.spring.bean.packages = []
 // whether to disable processing of multi part requests
 grails.web.disable.multipart=false
 
 // request parameters to mask when logging exceptions
 grails.exceptionresolver.params.exclude = ['password','password2','currentpassword']
 
 // configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
 grails.hibernate.cache.queries = false
 
 // configure passing transaction's read-only attribute to Hibernate session, queries and criterias
 // set "singleSession = false" OSIV mode in hibernate configuration after enabling
 grails.hibernate.pass.readonly = false
 // configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
 grails.hibernate.osiv.readonly = false
 
 
 environments {
	 development {
		 grails.logging.jul.usebridge = true
	 }
	 production {
		 grails.logging.jul.usebridge = false
	 }
 }
 
 
 
/**
 * API & CORS config
 */
cors.url.pattern = '/api/*'
cors.headers = ['Access-Control-Allow-Origin': '*']

/**
 * Application properties
 */
//Which class to use as the SignalPath run context
unifina.globals.className = "com.unifina.utils.Globals"
// Where to send email reports
unifina.reports.recipient = "henri.pihkala@streamr.com"

/**
 * Task config
 */
// How many task worker threads to launch on startup
unifina.task.workers = 0
unifina.task.messageQueue = "streamr-tasks"

/**
 * Data feed config
 */
// Cache data files locally?
unifina.feed.useCache = false
// Base dir for caching
unifina.feed.cachedir = System.getProperty("java.io.tmpdir")
// Default file storage adapter
unifina.feed.fileStorageAdapter = "com.unifina.feed.file.S3FileStorageAdapter"


/**
 * Aid IP address discovery by defining acceptable IP address prefixes (or empty if anything goes)
 */
streamr.ip.address.prefixes = ["192.168.10.", "192.168."]
environments {
	production {
		streamr.ip.address.prefixes = []
	}
}

/**
 * UI update server address
 */
streamr.ui.server = System.getProperty("streamr.ui.server") ?: "dev.unifina:8889"
environments {
	production {
		streamr.ui.server = System.getProperty("streamr.ui.server") ?: "api.streamr.com"
	}
}

/**
 * HTTP API server address
 */
streamr.http.api.server = System.getProperty("streamr.http.api.server") ?: "http://dev.unifina:8888"
environments {
	production {
		streamr.http.api.server = System.getProperty("streamr.ui.server") ?: "http://api.streamr.com"
	}
}

/**
 * Kafka config
 */
unifina.kafka.metadata.broker.list = "192.168.10.21:9092"
unifina.kafka.zookeeper.connect = "192.168.10.21:2181"
unifina.kafka.producer.type = "async"
unifina.kafka.queue.buffering.max.ms = "100"
unifina.kafka.retry.backoff.ms = "500"
unifina.kafka.serializer.class = "kafka.serializer.StringEncoder"
unifina.kafka.request.required.acks = "0"
unifina.kafka.group.id = "streamr"
environments {
	production {
		unifina.kafka.metadata.broker.list = "ip-10-16-207-139.ec2.internal:9092"
		unifina.kafka.zookeeper.connect = "ip-10-16-207-139.ec2.internal:2181"
	}
}

environments {
	development {

	}
	test {
		// Required for functional tests for backtesting
		unifina.task.workers = 1
	}
	production {
		// For Amazon
		unifina.task.workers = 1
	}

}

/**
 * Spring security config
 */

streamr.user.defaultFeeds = [7]
streamr.user.defaultModulePackages = [1]
grails.plugin.springsecurity.ui.register.defaultRoleNames = ["ROLE_USER", "ROLE_LIVE"]

grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.unifina.domain.security.SecUser'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.unifina.domain.security.SecUserSecRole'
grails.plugin.springsecurity.authority.className = 'com.unifina.domain.security.SecRole'

grails.plugin.springsecurity.rememberMe.cookieName = 'streamr_remember_me'
grails.plugin.springsecurity.rememberMe.key = 'IfYouCanDreamItYouCanStreamIt'
grails.plugin.springsecurity.password.algorithm = 'bcrypt'
grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.successHandler.defaultTargetUrl = '/canvas'
grails.plugin.springsecurity.ui.encodePassword = true
grails.plugin.springsecurity.ui.password.minLength = 8

// Due to https://jira.grails.org/browse/GPSPRINGSECURITYCORE-253 errorPage needs to be 
// set to null and 403 mapped in UnifinaCorePluginUrlMappings
grails.plugin.springsecurity.adh.errorPage = null

grails.plugin.springsecurity.securityConfigType = 'Annotation'

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	'/atmosphere/**': 		 ['ROLE_USER'],
	'/user/**':            ['ROLE_ADMIN'],
	'/register/**':				 ['IS_AUTHENTICATED_ANONYMOUSLY'],
	'/webcomponents/*':				 ['IS_AUTHENTICATED_ANONYMOUSLY'],
	'/*':				 ['IS_AUTHENTICATED_ANONYMOUSLY']
]

/**
 * Miscellaneous
 */
tomcat.nio = true // in run-app or test-app


// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line
// GSP settings
grails {
	views {
		gsp {
			encoding = 'UTF-8'
			htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
			codecs {
				expression = 'html' // escapes values inside null
				scriptlet = 'none' // escapes output from scriptlets in GSPs
				taglib = 'none' // escapes output from taglibs
				staticparts = 'none' // escapes output from static template parts
			}
		}
		// escapes all not-encoded output at final stage of outputting
		filteringCodecForContentType {
			//'text/html' = 'html'
		}
	}
}
remove this line */
