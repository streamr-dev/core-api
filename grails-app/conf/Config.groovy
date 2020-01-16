import com.unifina.service.NodeService
import com.unifina.utils.PropertiesUtil

/**
 * Base URL
 */
// Write it to a variable to allow it to be referenced elsewhere in this file
def baseUrl = System.getProperty("streamr.url") ?: "http://localhost"
environments {
	production {
		baseUrl = System.getProperty("streamr.url") ?: "https://streamr.network"
	}
}
grails.serverURL = baseUrl

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

	// No need to log all exceptions thrown in API calls. For example, InvalidAPIKeyExceptions easily pollute the logs.
	fatal 'org.codehaus.groovy.grails.web.errors.GrailsExceptionResolver'

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
		'org.codehaus.groovy.grails.domain.GrailsDomainClassCleaner'

	// Turn on debug logging for a few classes to debug join issue in prod
	debug 'com.streamr.client',
		'com.unifina.service.CommunityJoinRequestService',
		'com.unifina.service.StreamrClientService'
}

/**
 * Community Product Server configuration
 */
streamr.cps.url = System.getProperty("streamr.cps.url") ?: "http://localhost:8085/communities/"

// CPS Apache HTTP Client configuration

// Timeout in milliseconds until a connection is established
streamr.cps.connectTimeout = 60 * 1000
//  Timeout in milliseconds used when requesting a connection from the connection manager
streamr.cps.connectionRequestTimeout = 60 * 1000
// Defines the socket timeout (SO_TIMEOUT) in milliseconds, which is the timeout for waiting for data or, put differently, a maximum period inactivity between two consecutive data packets).
// A timeout value of zero is interpreted as an infinite timeout. A negative value is interpreted as undefined (system default if applicable).
streamr.cps.socketTimeout = 60 * 1000
// Maximum number of connections in the pool
streamr.cps.maxConnTotal = 400
// Maximum number of connections per route
streamr.cps.maxConnPerRoute = 200

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
cors.url.pattern = ['/api/*', '/contact/send', '/profile/*', '/logout/*', '/login/*', '/j_spring_security_check', '/canvas', '/logout', '/j_spring_security_logout']
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

/**
 * Node IP address config. Autodetected if not set.
 */
streamr.engine.node.ip = System.getProperty("streamr.engine.node.ip")

/**
 * Streamr API URLs
 */
streamr.api.websocket.url = System.getProperty("streamr.api.websocket.url") ?: "${baseUrl.replaceFirst("http", "ws")}/api/v1/ws"
streamr.api.http.url = System.getProperty("streamr.api.http.url") ?: "${baseUrl}/api/v1"

streamr.ethereum.datacoinAddress = System.getProperty("streamr.ethereum.datacoinAddress", "0x0cf0ee63788a0849fe5297f3407f701e122cc023")

/**
 * Ethereum networks configuration (RPC urls)
 *
 * Can be configured via JVM system properties. Example command line flags:
 *
 * -Dstreamr.ethereum.defaultNetwork=someNetwork
 * -Dstreamr.ethereum.networks.someNetwork=http://some-network-rpc-url
 * -Dstreamr.ethereum.networks.anotherNetwork=http://some-network-rpc-url
 */
streamr.ethereum.networks = PropertiesUtil.matchingPropertiesToMap("streamr.ethereum.networks.", System.getProperties()) ?: [ local: "http://localhost:8545" ]
streamr.ethereum.wss = PropertiesUtil.matchingPropertiesToMap("streamr.ethereum.wss.", System.getProperties()) ?: [ local: "ws://localhost:8545" ]
// Ethereum identity of this instance. Don't use this silly development private key for anything.
streamr.ethereum.nodePrivateKey = "".equals(System.getProperty("streamr.ethereum.nodePrivateKey", "")) ? "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF" : System.getProperty("streamr.ethereum.nodePrivateKey")
streamr.ethereum.defaultNetwork = System.getProperty("streamr.ethereum.defaultNetwork") ?: streamr.ethereum.networks.keySet().first()

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
streamr.cassandra.username = System.getProperty("streamr.cassandra.username")
streamr.cassandra.password = System.getProperty("streamr.cassandra.password")

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
grails.plugin.springsecurity.successHandler.defaultTargetUrl = '/'
grails.plugin.springsecurity.auth.loginFormUrl = '/'
grails.plugin.springsecurity.auth.ajaxLoginFormUrl = '/'
grails.plugin.springsecurity.ui.encodePassword = true
grails.plugin.springsecurity.ui.password.minLength = 8

// Due to https://jira.grails.org/browse/GPSPRINGSECURITYCORE-253 errorPage needs to be
// set to null and 403 mapped in UrlMappings
grails.plugin.springsecurity.adh.errorPage = null

grails.plugin.springsecurity.securityConfigType = 'Annotation'

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
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
unifina.email.shareInvite.subject = "%USER% wants to share a %RESOURCE% with you via Streamr Core"

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

streamr.metrics.numberOfSessions = "Tomcat:type=Manager,context=/streamr-core,host=localhost"
environments {
	production {
		streamr.metrics.numberOfSessions = "Catalina:type=Manager,context=/,host=localhost"
	}
}

/**
 * Signup Configs
 */
streamr.signup.requireCaptcha = (System.getProperty("streamr.signup.requireCaptcha") ? Boolean.parseBoolean(System.getProperty("streamr.signup.requireCaptcha")) : false)

/**
 * Streamr engine-and-editor nodes
 */
streamr.engine.nodes = System.getProperty("streamr.engine.nodes") ? Arrays.asList(System.getProperty("streamr.engine.nodes").split(",")) : [new NodeService().getIPAddress([streamr: [node: [ip: System.getProperty("streamr.engine.node.ip")]]])]

/**
 * Miscellaneous
 */
tomcat.nio = true // in run-app or test-app
