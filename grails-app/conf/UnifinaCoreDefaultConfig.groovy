import com.unifina.data.KafkaPartitioner

/*****
 * This config file gets merged with the application config file.
 * The application config file can override anything defined here.
 */

def prodBaseUrl = System.getProperty("streamr.url") ?: "https://www.streamr.com"

environments {
	production {
		grails.serverURL = prodBaseUrl
	}
}

/**
 * Logging config
 */
log4j = {
	// Example of changing the log pattern for the default console
	// appender:
	//
	appenders {
		console name:'stdout'
	}

	root {
		info 'stdout'
	}

	error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
			'org.codehaus.groovy.grails.web.pages', //  GSP
			'org.codehaus.groovy.grails.web.sitemesh', //  layouts
			'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
			'org.codehaus.groovy.grails.web.mapping', // URL mapping
			'org.codehaus.groovy.grails.commons', // core / classloading
			'org.codehaus.groovy.grails.plugins', // plugins
			'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
			'org.springframework',
			'org.hibernate',
			'net.sf.ehcache.hibernate'

	warn   'org.mortbay.log',
			'org.apache.zookeeper',
			'org.codehaus.groovy.grails.domain.GrailsDomainClassCleaner',
			'kafka.consumer.ConsumerConfig'
			'org.apache.kafka.clients.consumer.ConsumerConfig'
			'kafka.producer.ProducerConfig'
			'org.apache.kafka.clients.producer.ProducerConfig'
}

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
 grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*', "/js/polymer/*", "/js/leaflet", "/misc/*"]
 grails.resources.adhoc.includes = ['/images/**', '/css/**', '/js/**', '/plugins/**', '/misc/**']

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
		 grails.resources.processing.enabled = false
		 grails.resources.mappers.bundle.excludes = ['**/*.*']
		 grails.resources.mappers.hashandcache.excludes = ['**/*.*']
		 grails.resources.mappers.zip.excludes = ['**/*.*']
		 grails.resources.processing.excludes = ['**/*.js']
		 grails.resources.mappers.uglifyjs.excludes = ['**/*.*']
	 }
	 production {
		 grails.resources.mappers.uglifyjs.excludes = ['**/*.min.js', '**/*-min.js']
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
 * Tour config
 */
streamr.tours.enabled = true

/**
 * Migration config
 */
grails.plugin.databasemigration.updateOnStart = true
grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']
grails.plugin.databasemigration.updateOnStartContexts = ['default'] // a context needs to be specified, otherwise all changesets will run. changesets with no context will always run.

environments {
	test {
		grails.plugin.databasemigration.updateOnStartContexts = ['test'] // changesets with no context will always run.
	}
}

/**
 * API & CORS config
 */
cors.url.pattern = '/api/*'
cors.headers = ['Access-Control-Allow-Origin': '*']
streamr.apiKey.revokeNotificationStream = "revoked-api-keys"

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
unifina.task.workers = 1
unifina.task.messageQueue = "streamr-tasks"

environments {
	development {
		unifina.task.workers = 0
	}
}

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
 * com.unifina.feed.file.S3FileStorageAdapter config
 */
// The following are used with S3FileStorageAdapter
unifina.feed.s3FileStorageAdapter.accessKey = "AKIAJ5FFWRZLSQB6ASIQ"
unifina.feed.s3FileStorageAdapter.secretKey = "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"
unifina.feed.s3FileStorageAdapter.bucket = "streamr-data-dev"
environments {
	production {
		unifina.feed.s3FileStorageAdapter.bucket = "streamr-data-us"
	}
}


/**
 * Aid IP address discovery by defining acceptable IP address prefixes (or empty if anything goes)
 */
streamr.ip.address.prefixes = System.getProperty("streamr.ip.address.prefixes") ? Arrays.asList(System.getProperty("streamr.ip.address.prefixes").split(",")) : ["192.168.10.", "192.168.", "10."]
environments {
	production {
		streamr.ip.address.prefixes = []
	}
}

/**
 * UI update server address
 */
streamr.ui.server = System.getProperty("streamr.ui.server") ?: "ws://dev.streamr/api/v1/ws"
environments {
	production {
		streamr.ui.server = System.getProperty("streamr.ui.server") ?: "${prodBaseUrl.replaceFirst("http", "ws")}/api/v1/ws"
	}
}

/**
 * HTTP API server address
 */
streamr.http.api.server = System.getProperty("streamr.http.api.server") ?: "http://dev.streamr/api/v1"
environments {
	production {
		streamr.http.api.server = System.getProperty("streamr.ui.server") ?: "${prodBaseUrl}/api/v1"
	}
}

/**
 * Kafka config
 */
streamr.kafka.bootstrap.servers = System.getProperty("streamr.kafka.bootstrap.servers") ?: "192.168.10.21:9093"
streamr.kafka.zookeeper.connect = System.getProperty("streamr.kafka.zookeeper.connect") ?: "192.168.10.21:2182"
streamr.kafka.producer.type = "async"
streamr.kafka.queue.buffering.max.ms = "100"
streamr.kafka.retry.backoff.ms = "500"
streamr.kafka.value.serializer = org.apache.kafka.common.serialization.ByteArraySerializer.getName()
streamr.kafka.key.serializer = org.apache.kafka.common.serialization.StringSerializer.getName()
streamr.kafka.partitioner.class = KafkaPartitioner.class.getName()
streamr.kafka.request.required.acks = "0"
streamr.kafka.dataTopic = "data-dev"

environments {
	production {
		streamr.kafka.dataTopic = "data-prod"
		streamr.kafka.bootstrap.servers = System.getProperty("streamr.kafka.bootstrap.servers") ?: "kafka1:9092"
		streamr.kafka.zookeeper.connect = System.getProperty("streamr.kafka.zookeeper.connect") ?: "zk1:2181"
	}
}

/**
 * Redis config
 */
streamr.redis.hosts = (System.getProperty("streamr.redis.hosts") ? Arrays.asList(System.getProperty("streamr.redis.hosts").split(",")) : ["dev.streamr"])
streamr.redis.password = "AFuPxeVMwBKHV5Hm5SK3PkRZA"
environments {
	production {
		streamr.redis.hosts = (System.getProperty("streamr.redis.hosts") ? Arrays.asList(System.getProperty("streamr.redis.hosts").split(",")) : ["redis1"])
	}
}

/**
 * Cassandra config
 */
streamr.cassandra.hosts = (System.getProperty("streamr.cassandra.hosts") ? Arrays.asList(System.getProperty("streamr.cassandra.hosts").split(",")) : ["dev.streamr"])
streamr.cassandra.keySpace = System.getProperty("streamr.cassandra.keySpace") ?: "streamr_dev"

environments {
	production {
		streamr.cassandra.hosts = (System.getProperty("streamr.cassandra.hosts") ? Arrays.asList(System.getProperty("streamr.cassandra.hosts").split(",")) : ["cassandra1"])
		streamr.cassandra.keySpace = System.getProperty("streamr.cassandra.keySpace") ?: "streamr_prod"
	}
}
/**
 * Serialization config
 */
streamr.serialization.intervalInMillis = System.getProperty("streamr.serialization.intervalInMillis") ? Long.parseLong(System.getProperty("streamr.serialization.intervalInMillis")) : 5 * 60 * 1000
environments {
	test {
		streamr.serialization.intervalInMillis = 1000
	}
}

/**
 * Spring security config
 */

grails.plugin.springsecurity.ui.register.defaultRoleNames = ["ROLE_USER", "ROLE_LIVE"]

grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.unifina.domain.security.SecUser'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.unifina.domain.security.SecUserSecRole'
grails.plugin.springsecurity.authority.className = 'com.unifina.domain.security.SecRole'

grails.plugin.springsecurity.rememberMe.enabled = true
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
 * Email config
 */
grails {
	mail {
		host = "email-smtp.us-east-1.amazonaws.com"
		port = 465
		username = "AKIAIV4PGPKXNAGNDFQQ"
		password = "AqH4L/VferJlG0KExv0D8pEvJW6LR7LC6Q4VqzVZAbTS"
		props = ["mail.smtp.auth":"true",
				 "mail.smtp.socketFactory.port":"465",
				 "mail.smtp.starttls.enable":"true",
				 "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
				 "mail.smtp.socketFactory.fallback":"false"]
	}
}

unifina.email.sender = "contact@streamr.com"
unifina.email.waitForInvite.subject = "Thanks for signing up for Streamr"
unifina.email.registerLink.subject = "Streamr signup link"
unifina.email.invite.subject = "Invitation to Streamr"
unifina.email.welcome.subject = "Welcome to Streamr"
unifina.email.feedback.recipient = "contact@streamr.com"
unifina.email.forgotPassword.subject = "Streamr Password Reset"
unifina.email.shareInvite.subject = "%USER% shared a document with you in Streamr"

/**
 * Signup Configs
 */
streamr.signup.requireInvite = (System.getProperty("streamr.signup.requireInvite") ? Boolean.parseBoolean(System.getProperty("streamr.signup.requireInvite")) : false)

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
