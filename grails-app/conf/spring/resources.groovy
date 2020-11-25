import com.mchange.v2.c3p0.ComboPooledDataSource
import com.streamr.api.client.StreamrClientProvider
import com.unifina.provider.S3FileUploadProvider
import com.unifina.security.BCryptPasswordEncoder
import com.unifina.utils.CustomEditorRegistrar

// Place your Spring DSL code here
beans = {
	myOwnCustomEditorRegistrar(CustomEditorRegistrar)

	passwordEncoder(BCryptPasswordEncoder, grailsApplication.config.streamr.encryption.bcrypt.logrounds)

	fileUploadProvider(S3FileUploadProvider,
		(String) grailsApplication.config.streamr.fileUpload.s3.region,
		(String) grailsApplication.config.streamr.fileUpload.s3.bucket
	)

	streamrClient(StreamrClientProvider,
		(String) grailsApplication.config.streamr.api.http.url
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
