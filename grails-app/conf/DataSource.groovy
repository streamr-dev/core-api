dataSource {
	pooled = true
	driverClassName = "com.mysql.jdbc.Driver"
	dialect = 'org.hibernate.dialect.MySQL5InnoDBDialect'
	properties {
		initialSize = 2
		maxActive = 50 // -1
		minIdle = 2
		maxIdle = 25
		maxWait = 30000
		maxAge = 10 * 60000
		minEvictableIdleTimeMillis=60000 // 1800000
		timeBetweenEvictionRunsMillis=60000 //1800000, default: 5000
		validationQuery = "SELECT 1"
		validationQueryTimeout = 3
		validationInterval = 15000
		testOnBorrow = true
		testWhileIdle = true
		testOnReturn = false
	}
}
hibernate {
	cache.use_second_level_cache = true
	cache.use_query_cache = true
	cache.region.factory_class = 'org.hibernate.cache.SingletonEhCacheRegionFactory'
	// Prevent complex queries from taking a lot of memory
	query.plan_cache_max_strong_references = 1 // default 128
	query.plan_cache_max_soft_references = 2048 // default 2048
}
// environment specific settings
environments {
    development {
        dataSource {
			// if grails.dev.database not defined, throw an exception
			def dbDefaultName = 'core_dev'
			def dbName = System.getProperty('grails.dev.database') ?: dbDefaultName

			println "Using database: $dbName"

			if (!System.getProperty('grails.dev.database'))
				throw new RuntimeException("Please run scripts/copy-dev-db.sh YOURNAME to make a personal copy of the dev db, then run grails with this command line argument: -Dgrails.dev.database=${dbDefaultName}_YOURNAME")

			username = "unifina-dev"
			password = "2PpJA2vJ"
            url = "jdbc:mysql://192.168.10.21:3306/${dbName}?useLegacyDatetimeCode=false"
        }
    }
    test {
        dataSource {
			// If not in jenkins and if grails.test.database not defined, throw an exception
			def dbDefaultName = 'core_test'
			def dbName = System.getProperty('grails.test.database') ?: dbDefaultName
			def testPhase = System.getProperty('grails.test.phase')

			println "Using database: $dbName"
			println "Test phase: $testPhase"

			if (System.getenv()['BUILD_NUMBER']==null && !System.getProperty('grails.test.database') && testPhase!=null && testPhase!="unit")
				throw new RuntimeException("Please run scripts/copy-test-db.sh YOURNAME to make a personal copy of the test db, then run grails with this command line argument: -Dgrails.test.database=${dbDefaultName}_YOURNAME")
			
			username = "unifina-test"
			password = "HqTQK9kB"
            url = "jdbc:mysql://192.168.10.21:3306/${dbName}?useLegacyDatetimeCode=false"
        }
    }
    production {
		// The core is only deployed in production for on-site customer installations. (Our cloud deployment uses streamr-webapp)
		// The settings come from system properties.
		dataSource {
			username = System.getProperty('streamr.database.user') ?: "streamr-prod"
			password = System.getProperty('streamr.database.password') ?: "Trez2tuV"
			url = "jdbc:mysql://${System.getProperty('streamr.database.host')}/${System.getProperty('streamr.database.name')}?useLegacyDatetimeCode=false"
		}
    }
}
