dataSource {
	pooled = true
	driverClassName = "com.mysql.jdbc.Driver"
	dialect = 'org.hibernate.dialect.MySQL5InnoDBDialect'
}
hibernate {
	cache.use_second_level_cache = true
	cache.use_query_cache = true
	cache.region.factory_class = 'org.hibernate.cache.SingletonEhCacheRegionFactory'
	// cache.region.factory_class = 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory' // Hibernate 4
	// Prevent complex queries from taking a lot of memory
	query.plan_cache_max_strong_references = 1 // default 128
	query.plan_cache_max_soft_references = 2048 // default 2048
}
// environment specific settings
environments {
    development {
        dataSource {
			logSql = Boolean.parseBoolean(System.getProperty('logSql') ?: 'false')
			formatSql = true
			username = System.getProperty('streamr.database.user', 'root')
			password = System.getProperty('streamr.database.password', 'password')
			url = "jdbc:mysql://${System.getProperty('streamr.database.host', '127.0.0.1:3306')}/${System.getProperty('streamr.database.name', 'core_dev')}?useLegacyDatetimeCode=false"
        }
    }
    test {
        dataSource {
			//logSql = true
			formatSql = true
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
			password = System.getProperty('streamr.database.password')
			url = "jdbc:mysql://${System.getProperty('streamr.database.host') ?: "mysql"}/${System.getProperty('streamr.database.name', 'streamr_prod')}?useLegacyDatetimeCode=false"
		}
    }
}
