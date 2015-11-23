import grails.util.Environment;

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
			// If not in jenkins and if grails.test.database not defined, throw an exception
			if (System.getenv()['BUILD_NUMBER']==null && !System.getProperty('grails.test.database'))
				throw new RuntimeException("Please run scripts/copy-test-db.sh to make a personal copy of the test db, then run grails with this command line argument: -Dgrails.test.database=core_test_YOURNAME")
			
			def dbName = System.getProperty('grails.test.database') ?: 'core_test'
			println "Using database: $dbName"
			
			username = "unifina-test"
			password = "HqTQK9kB"
            dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:mysql://192.168.10.21:3306/${dbName}?useLegacyDatetimeCode=false"
        }
    }
    production {
		// There is no prod database for core
    }
}
