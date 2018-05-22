import com.unifina.provider.S3FileUploadProvider
import com.unifina.security.RedirectAppendingAuthenticationEntryPoint
import com.unifina.utils.AjaxAwareRequestMatcher
import com.unifina.utils.CustomEditorRegistrar
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.web.savedrequest.HttpSessionRequestCache

// Place your Spring DSL code here
beans = {
	myOwnCustomEditorRegistrar(CustomEditorRegistrar)

	requestCache(HttpSessionRequestCache) {
		portResolver = ref('portResolver')
		createSessionAllowed = true
		requestMatcher = new AjaxAwareRequestMatcher()
	}

	// The bean that redirects to /login/auth when trying to access a page which requires authentication
	authenticationEntryPoint(RedirectAppendingAuthenticationEntryPoint, SpringSecurityUtils.getSecurityConfig().auth.loginFormUrl) {
		defaultRedirectURI = SpringSecurityUtils.getSecurityConfig().successHandler.defaultTargetUrl
		portMapper = ref('portMapper')
		portResolver = ref('portResolver')
		linkGenerator = ref('grailsLinkGenerator')
	}

	fileUploadProvider(S3FileUploadProvider,
		(String) grailsApplication.config.streamr.fileUpload.s3.region,
		(String) grailsApplication.config.streamr.fileUpload.s3.bucket
	)
}
