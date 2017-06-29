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
			username = System.getProperty('streamr.database.user', 'root')
			password = System.getProperty('streamr.database.password', 'password')
			url = "jdbc:mysql://${System.getProperty('streamr.database.host', '127.0.0.1:3306')}/${System.getProperty('streamr.database.name', 'core_dev')}?useLegacyDatetimeCode=false"
        }
    }
    test {
        dataSource {
			username = System.getProperty('streamr.database.user', 'root')
			password = System.getProperty('streamr.database.password', 'password')
			url = "jdbc:mysql://${System.getProperty('streamr.database.host', '127.0.0.1:3306')}/${System.getProperty('streamr.database.name', 'core_test')}?useLegacyDatetimeCode=false"
        }
    }
    production {
		// The core is only deployed in production for on-site customer installations. (Our cloud deployment uses streamr-webapp)
		// The settings come from system properties.
		dataSource {
			username = System.getProperty('streamr.database.user') ?: "streamr-prod"
			password = System.getProperty('streamr.database.password') ?: "Trez2tuV"
			url = "jdbc:mysql://${System.getProperty('streamr.database.host') ?: "mysql"}/${System.getProperty('streamr.database.name')}?useLegacyDatetimeCode=false"
		}
    }
}
