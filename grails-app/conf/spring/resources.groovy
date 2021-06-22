import com.mchange.v2.c3p0.ComboPooledDataSource
import com.streamr.s3.S3ClientDefault

beans = {
	s3Client(S3ClientDefault,
		(String) grailsApplication.config.streamr.fileUpload.s3.region,
		(String) grailsApplication.config.streamr.fileUpload.s3.bucket
	)

	dataSource(ComboPooledDataSource) { args ->
		args.destroyMethod = "close"
		user = grailsApplication.config.dataSource.username
		password = grailsApplication.config.dataSource.password
		driverClass = grailsApplication.config.dataSource.driverClassName
		jdbcUrl = grailsApplication.config.dataSource.url
		// Options below can be tweaked for performance
		maxPoolSize = 100
		testConnectionOnCheckin = true
		testConnectionOnCheckout = false
		idleConnectionTestPeriod = 30
	}
}
