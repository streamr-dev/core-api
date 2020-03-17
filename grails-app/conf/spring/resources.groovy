import com.streamr.api.client.StreamrClientProvider
import com.unifina.provider.S3FileUploadProvider
import com.unifina.utils.CustomEditorRegistrar

// Place your Spring DSL code here
beans = {
	myOwnCustomEditorRegistrar(CustomEditorRegistrar)

	fileUploadProvider(S3FileUploadProvider,
		(String) grailsApplication.config.streamr.fileUpload.s3.region,
		(String) grailsApplication.config.streamr.fileUpload.s3.bucket
	)

	streamrClient(StreamrClientProvider,
		(String) grailsApplication.config.streamr.api.http.url
	)
}
