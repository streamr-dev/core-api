import com.google.gson.Gson
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
 * Grails configuration
 */

grails.project.groupId = appName

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
		grails.resources.mappers.uglifyjs.excludes = ['**/*.min.js', '**/*-min.js', '**/*.bundle.js', '**/*-compressed.js']
	}
}

// See WebpackTagLib.groovy
webpack.bundle.dir = System.getProperty("webpack.bundle.location") ?: '/webpack-bundles'
webpack.jsFiles.metadataKey = 'webpack.jsFiles'
webpack.cssFiles.metadataKey = 'webpack.cssFiles'

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
 * Logging config
 */
log4j.main = {
	// Example of changing the log pattern for the default console
	// appender:
	//
	appenders {
		console name:'stdout'
	}

	root {
		info 'stdout'
	}

	error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
		'org.codehaus.groovy.grails.web.pages', //  GSP
		'org.codehaus.groovy.grails.web.sitemesh', //  layouts
		'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
		'org.codehaus.groovy.grails.web.mapping', // URL mapping
		'org.codehaus.groovy.grails.commons', // core / classloading
		'org.codehaus.groovy.grails.plugins', // plugins
		'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
		'org.springframework',
		'org.hibernate',
		'net.sf.ehcache.hibernate',
		'org.grails.datastore.mapping.core.DatastoreUtils'

	warn 'org.mortbay.log',
		'org.apache.zookeeper',
		'org.codehaus.groovy.grails.domain.GrailsDomainClassCleaner',
		'kafka.consumer.ConsumerConfig',
		'org.apache.kafka.clients.consumer.ConsumerConfig',
		'kafka.producer.ProducerConfig',
		'org.apache.kafka.clients.producer.ProducerConfig'
}

/**
 * Streamr cluster config
 */
streamr.cluster.internalPort = System.getProperty("streamr.cluster.internalPort") ? Integer.parseInt(System.getProperty("streamr.cluster.internalPort")) : 8081
streamr.cluster.internalProtocol = System.getProperty("streamr.cluster.internalProtocol") ?: "http"
environments {
	production {
		streamr.cluster.internalPort = System.getProperty("streamr.cluster.internalPort") ? Integer.parseInt(System.getProperty("streamr.cluster.internalPort")) : 8080
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
cors.url.pattern = ['/api/*', '/contact/send']
streamr.apiKey.revokeNotificationStream = "revoked-api-keys"

/**
 * Application properties
 */
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


/**
 * Aid IP address discovery by defining acceptable IP address prefixes (or empty if anything goes)
 */
streamr.ip.address.prefixes = System.getProperty("streamr.ip.address.prefixes") ? Arrays.asList(System.getProperty("streamr.ip.address.prefixes").split(",")) : ["192.168.10.", "192.168.", "10.", "172.18."]
environments {
	production {
		streamr.ip.address.prefixes = []
	}
}

/**
 * UI update server address
 */
streamr.ui.server = System.getProperty("streamr.ui.server") ?: "ws://127.0.0.1:8890/api/v1/ws"
environments {
	production {
		streamr.ui.server = System.getProperty("streamr.ui.server") ?: "${prodBaseUrl.replaceFirst("http", "ws")}/api/v1/ws"
	}
}

/**
 * HTTP API server address
 */
streamr.http.api.server = System.getProperty("streamr.http.api.server") ?: "http://127.0.0.1:8890/api/v1"
environments {
	production {
		streamr.http.api.server = System.getProperty("streamr.http.api.server") ?: "${prodBaseUrl}/api/v1"
	}
}

/**
 * Streamr-web3 Ethereum bridge address
 */
streamr.ethereum.defaultNetwork = "rinkeby"
streamr.ethereum.networks = System.getProperty("streamr.ethereum.networks") ? new Gson().fromJson(System.getProperty("streamr.ethereum.networks")) : [
	ropsten: "http://localhost:3000",
	rinkeby: "http://localhost:3001"
]
streamr.ethereum.rpcUrls = System.getProperty("streamr.ethereum.rpcUrls") ? new Gson().fromJson(System.getProperty("streamr.ethereum.rpcUrls")) : [
	ropsten: "http://localhost:8545",
	rinkeby: "http://localhost:8546"
]
environments {
	production {
		streamr.ethereum.networks = System.getProperty("streamr.ethereum.networks") ? new Gson().fromJson(System.getProperty("streamr.ethereum.networks")) : [
			ropsten: "http://ropsten:3000",
			rinkeby: "http://rinkeby:3001"
		]
		streamr.ethereum.rpcUrls = System.getProperty("streamr.ethereum.rpcUrls") ? new Gson().fromJson(System.getProperty("streamr.ethereum.rpcUrls")) : [
			ropsten: "http://ropsten:8545",
			rinkeby: "http://rinkeby:8546"
		]
	}
}

/**
 * Kafka config
 */
streamr.kafka.bootstrap.servers = System.getProperty("streamr.kafka.bootstrap.servers") ?: "127.0.0.1:9092"
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
streamr.redis.hosts = (System.getProperty("streamr.redis.hosts") ? Arrays.asList(System.getProperty("streamr.redis.hosts").split(",")) : ["127.0.0.1"])
streamr.redis.password = System.getProperty("streamr.redis.password") ?: ""
environments {
	production {
		streamr.redis.hosts = (System.getProperty("streamr.redis.hosts") ? Arrays.asList(System.getProperty("streamr.redis.hosts").split(",")) : ["redis1"])
		streamr.redis.password = System.getProperty("streamr.redis.password")
	}
}

/**
 * Cassandra config
 */
streamr.cassandra.hosts = (System.getProperty("streamr.cassandra.hosts") ? Arrays.asList(System.getProperty("streamr.cassandra.hosts").split(",")) : ["127.0.0.1"])
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
streamr.serialization.maxBytes = System.getProperty("streamr.serialization.maxBytes") ? Long.parseLong(System.getProperty("streamr.serialization.maxBytes")) : 1024 * 1024 * 8
environments {
	test {
		streamr.serialization.intervalInMillis = 1000
	}
}

/**
 * Encryption settings
 */
streamr.encryption.password = System.getProperty("streamr.encryption.password") ?: "password" // dev and test environments have a default password
environments {
	production {
		streamr.encryption.password = System.getProperty("streamr.encryption.password") // in production, the system property must be set
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
grails.plugin.springsecurity.rememberMe.key = System.getProperty("grails.plugin.springsecurity.rememberMe.key") ?: 'IfYouCanDreamItYouCanStreamIt'
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
	'/user/**':           ['ROLE_ADMIN'],
	'/register/**':       ['IS_AUTHENTICATED_ANONYMOUSLY'],
	'/webcomponents/*':   ['IS_AUTHENTICATED_ANONYMOUSLY'],
	'/webpack-bundles/*': ['IS_AUTHENTICATED_ANONYMOUSLY'],
	'/*':                 ['IS_AUTHENTICATED_ANONYMOUSLY']
]

/**
 * Email config
 */
grails {
	mail {
		host = System.getProperty("grails.mail.host")
		port = System.getProperty("grails.mail.port")
		username = System.getProperty("grails.mail.username")
		password = System.getProperty("grails.mail.password")
		props = ["mail.smtp.auth":"true",
				 "mail.smtp.socketFactory.port":"465",
				 "mail.smtp.starttls.enable":"true",
				 "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
				 "mail.smtp.socketFactory.fallback":"false"]

		environments {
			development {
				host = System.getProperty("grails.mail.host") ?: "127.0.0.1"
				port = System.getProperty("grails.mail.port") ?: 25
				props = []
			}
			test {
				host = System.getProperty("grails.mail.host") ?: "127.0.0.1"
				port = System.getProperty("grails.mail.port") ?: 25
				props = []
			}
		}
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
 * Recaptcha config
 */

recaptcha.verifyUrl = "https://www.google.com/recaptcha/api/siteverify"

environments {
	// Same keys used for both dev and test
	development {
		recaptchav2.sitekey = "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI"
		recaptchav2.secret = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe"
		recaptchainvisible.sitekey = "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI"
		recaptchainvisible.secret = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe"
	}
	test {
		recaptchav2.sitekey = "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI"
		recaptchav2.secret = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe"
		recaptchainvisible.sitekey = "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI"
		recaptchainvisible.secret = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe"
	}
	production {
		recaptchav2.sitekey = System.getProperty("recaptchav2.sitekey")
		recaptchainvisible.sitekey = System.getProperty("recaptchainvisible.sitekey")
		recaptchav2.secret = System.getProperty("recaptchav2.secret")
		recaptchainvisible.secret = System.getProperty("recaptchainvisible.secret")
	}
}

/**
 * S3 File upload
 */
streamr.fileUpload.s3.region = System.getProperty("streamr.fileUpload.s3.region") ?: "eu-west-1"
streamr.fileUpload.s3.bucket = System.getProperty("streamr.fileUpload.s3.bucket") ?: "streamr-dev-public"

environments {
	production {
		streamr.fileUpload.s3.region = System.getProperty("streamr.fileUpload.s3.region") ?: "eu-west-1"
		streamr.fileUpload.s3.bucket = System.getProperty("streamr.fileUpload.s3.bucket") ?: "streamr-public"
	}
}

/**
 * Signup Configs
 */
streamr.signup.requireInvite = (System.getProperty("streamr.signup.requireInvite") ? Boolean.parseBoolean(System.getProperty("streamr.signup.requireInvite")) : false)
streamr.signup.requireCaptcha = (System.getProperty("streamr.signup.requireCaptcha") ? Boolean.parseBoolean(System.getProperty("streamr.signup.requireCaptcha")) : false)

/**
 * Streamr engine-and-editor nodes
 */
streamr.nodes = System.getProperty("streamr.nodes") ? Arrays.asList(System.getProperty("streamr.nodes").split(",")) : ["127.0.0.1"]

/**
 * Miscellaneous
 */
tomcat.nio = true // in run-app or test-app
