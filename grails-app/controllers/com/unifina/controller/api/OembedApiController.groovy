package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import java.text.DecimalFormat

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class OembedApiController {

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index() {
		def url = URLDecoder.decode(params.url, "UTF-8")
		def width = params.maxwidth ? Double.parseDouble(params.maxwidth) : 400d
		def height = params.maxheight ? Double.parseDouble(params.maxheight) : 300d
		def format = params.format ? params.format.toLowerCase() : "json"
		def regex = /^https?:\/\/(www\.)?streamr\.com\/canvas\/embed\/([a-zA-Z0-9\-\_])+/
		if (!url.find(regex)) {
			throw new ApiException(404, "INVALID_URL", "Invalid url")
		}
		DecimalFormat df = new DecimalFormat()
		df.setDecimalSeparatorAlwaysShown(false) // To translate 200.0 to 200 but to keep 2.23 as 2.23
		def map = [
				url   : url,
				width : width,
				height: height,
				html: "" +
					"<iframe " +
						"width=\"${df.format(width)}\" " +
						"height=\"${df.format(height)}\" " +
						"src=\"${url}\"" +
					"></iframe>",
				type: "rich",
				version: "1.0",
				provider_name: "Streamr",
				provider_url: "https://www.streamr.com"
				// We could also send the title and author of the canvas
		]
		if (format == "json") {
			render map as JSON
		} else {
			throw new ApiException(501, "INVALID_TYPE", "Invalid format")
		}
	}
}
