import com.unifina.provider.S3FileUploadProvider
import com.unifina.utils.AjaxAwareRequestMatcher
import com.unifina.utils.CustomEditorRegistrar
import org.springframework.security.web.savedrequest.HttpSessionRequestCache

// Place your Spring DSL code here
beans = {
	myOwnCustomEditorRegistrar(CustomEditorRegistrar)

	requestCache(HttpSessionRequestCache) {
		portResolver = ref('portResolver')
		createSessionAllowed = true
		requestMatcher = new AjaxAwareRequestMatcher()
	}

	fileUploadProvider(S3FileUploadProvider,
		(String) grailsApplication.config.streamr.fileUpload.s3.region,
		(String) grailsApplication.config.streamr.fileUpload.s3.bucket
	)
}
