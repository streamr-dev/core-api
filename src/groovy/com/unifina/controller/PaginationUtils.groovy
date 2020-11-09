package com.unifina.controller

import com.unifina.service.ListParams
import javax.servlet.http.HttpServletResponse
import grails.compiler.GrailsCompileStatic
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import grails.util.Holders

@GrailsCompileStatic
class PaginationUtils {

	static void setHint(HttpServletResponse response, ListParams listParams, int numOfResults, Map params) {
		String link = createHint(listParams, numOfResults, params)
		if (link != null) {
			response.addHeader("Link", link)
		}
	}

	/**
	 * Generate link to more results in API index() methods
	 */
	private static String createHint(ListParams listParams, int numOfResults, Map params) {
		if (numOfResults == listParams.max) {
			Map<String, Object> paramMap = listParams.toMap()
			Integer offset = listParams.offset + listParams.max
			paramMap.put("offset", offset)
			LinkGenerator grailsLinkGenerator = Holders.getApplicationContext().getBean(LinkGenerator)
			String url = grailsLinkGenerator.link(
				controller: params.controller,
				action: params.action,
				absolute: true,
				params: paramMap.findAll { k, v -> v } // remove null valued entries
			)
			return "<${url}>; rel=\"more\""
		}
		return null
	}
}
