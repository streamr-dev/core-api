import com.streamr.core.utils.PropertiesUtil

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
	all: '*/*', // 'all' maps to '*' or the first available format in withFormat
	atom: 'application/atom+xml',
	css: 'text/css',
	csv: 'text/csv',
	form: 'application/x-www-form-urlencoded',
	html: ['text/html', 'application/xhtml+xml'],
	js: 'text/javascript',
	json: ['application/json', 'text/json'],
	multipartForm: 'multipart/form-data',
	rss: 'application/rss+xml',
	text: 'text/plain',
	hal: ['application/hal+json', 'application/hal+xml'],
	xml: ['text/xml', 'application/xml']
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
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password', 'password2', 'currentpassword']

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
		console name: 'stdout'
	}

	root {
		info 'stdout'
	}

	// No need to log all exceptions thrown in API calls. For example, invalid login attemps easily pollute the logs.
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

	debug 'com.streamr.core.service.DataUnionJoinRequestService',
		'com.streamr.core.service.PermissionService'
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
cors.url.pattern = [
	'/api/*', // Streamr REST API
]
cors.headers = ['Access-Control-Allow-Headers': 'origin, authorization, accept, content-type, x-requested-with, Streamr-Client'] // allow custom Streamr-Client header in CORS requests

/**
 * Streamr API URLs
 */
streamr.api.websocket.url = System.getProperty("streamr.api.websocket.url") ?: "${baseUrl.replaceFirst("http", "ws")}/api/v2/ws"
streamr.api.http.url = System.getProperty("streamr.api.http.url") ?: "${baseUrl}/api/v2"

streamr.ethereum.datacoinAddress = System.getProperty("streamr.ethereum.datacoinAddress", "0x0cf0ee63788a0849fe5297f3407f701e122cc023")

streamr.ethereum.streamRegistryChain = System.getProperty("streamr.ethereum.streamRegistryChain", "sidechain")
streamr.ethereum.streamRegistryAddress = System.getProperty("streamr.ethereum.streamRegistryAddress", "0x6cCdd5d866ea766f6DF5965aA98DeCCD629ff222")

/**
 * Ethereum networks configuration (RPC urls)
 *
 * Can be configured via JVM system properties. Example command line flags:
 *
 * -Dstreamr.ethereum.defaultNetwork=someNetwork
 * -Dstreamr.ethereum.networks.someNetwork=http://some-network-rpc-url
 * -Dstreamr.ethereum.networks.anotherNetwork=http://some-network-rpc-url
 */
streamr.ethereum.networks = PropertiesUtil.matchingPropertiesToMap("streamr.ethereum.networks.", System.getProperties()) ?: [
	local: "http://localhost:8545",
	sidechain: "http://localhost:8546",
]
streamr.ethereum.wss = PropertiesUtil.matchingPropertiesToMap("streamr.ethereum.wss.", System.getProperties()) ?: [
	local: "ws://10.200.10.1:8450",
	sidechain: "ws://10.200.10.1:8451",
]
// Ethereum identity of this instance. Don't use this silly development private key for anything.
streamr.ethereum.nodePrivateKey = "".equals(System.getProperty("streamr.ethereum.nodePrivateKey", "")) ? "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF" : System.getProperty("streamr.ethereum.nodePrivateKey")
streamr.ethereum.defaultNetwork = System.getProperty("streamr.ethereum.defaultNetwork") ?: streamr.ethereum.networks.keySet().first()

// Data Union 2.0
streamr.dataunion.sidechainName = System.getProperty("streamr.dataunion.sidechainName") ?: "sidechain"
streamr.dataunion.mainnet.factory.address = System.getProperty("streamr.dataunion.mainnet.factory.address") ? System.getProperty("streamr.dataunion.mainnet.factory.address") : "0x4bbcBeFBEC587f6C4AF9AF9B48847caEa1Fe81dA"
streamr.dataunion.sidechain.factory.address = System.getProperty("streamr.dataunion.sidechain.factory.address") ? System.getProperty("streamr.dataunion.sidechain.factory.address") : "0x4A4c4759eb3b7ABee079f832850cD3D0dC48D927"

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
 * Email config
 */
grails {
	mail {
		host = System.getProperty("grails.mail.host")
		port = System.getProperty("grails.mail.port")
		username = System.getProperty("grails.mail.username")
		password = System.getProperty("grails.mail.password")
		props = ["mail.smtp.auth": "true",
			"mail.smtp.socketFactory.port": "465",
			"mail.smtp.starttls.enable": "true",
			"mail.smtp.socketFactory.class": "javax.net.ssl.SSLSocketFactory",
			"mail.smtp.socketFactory.fallback": "false"]

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
 * Miscellaneous
 */
tomcat.nio = true // in run-app or test-app
