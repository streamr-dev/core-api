import com.streamr.api.client.StreamrClientProvider
import com.unifina.provider.S3FileUploadProvider
import com.unifina.utils.CustomEditorRegistrar
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder

// Place your Spring DSL code here
beans = {
	myOwnCustomEditorRegistrar(CustomEditorRegistrar)

	passwordEncoder(BCryptPasswordEncoder, SpringSecurityUtils.securityConfig.password.bcrypt.logrounds)

	fileUploadProvider(S3FileUploadProvider,
		(String) grailsApplication.config.streamr.fileUpload.s3.region,
		(String) grailsApplication.config.streamr.fileUpload.s3.bucket
	)

	streamrClient(StreamrClientProvider,
		(String) grailsApplication.config.streamr.api.http.url
	)
}
