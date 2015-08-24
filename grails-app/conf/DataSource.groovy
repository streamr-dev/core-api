dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
	dialect = 'org.hibernate.dialect.MySQL5InnoDBDialect'
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
		// use the test db in dev as well
        dataSource {
			username = "unifina-test"
			password = "HqTQK9kB"
            dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:mysql://192.168.10.21:3306/core_test?useLegacyDatetimeCode=false"
        }
    }
    test {
        dataSource {
			username = "unifina-test"
			password = "HqTQK9kB"
            dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:mysql://192.168.10.21:3306/core_test?useLegacyDatetimeCode=false"
        }
    }
    production {
		// There is no prod database for core
    }
}
