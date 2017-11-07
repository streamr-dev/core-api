package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class OembedApiController {

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index() {
		def url = URLDecoder.decode(params.url, "UTF-8")
		def width = params.maxwidth ? Double.parseDouble(params.maxwidth) : 400
		def height = params.maxheight ? Double.parseDouble(params.maxheight) : 300
		def format = params.format ?: "json"
		def regex = /^https:\/\/(www\.)?streamr\.com\/canvas\/embed\/([a-zA-Z0-9\-\_])+/
		if (!url.find(regex)) {
			throw new ApiException(404, "INVALID_URL", "Invalid url")
		}
		def map = [
				url   : url,
				width : width,
				height: height,
				html: "" +
					"<iframe " +
						"width=\"${width}\" " +
						"height=\"${height}\"" +
						"src=\"${url}\"" +
					"/>",
				provider_name: "Streamr",
				provider_url: "https://www.streamr.com",
				// We could also send the title and author of the canvas
		]
		if (format == "json") {
			render map as JSON
		} else {
			throw new ApiException(501, "INVALID_TYPE", "Invalid format")
		}
	}
}
