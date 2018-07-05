package com.unifina.security

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class RedirectAppendingAuthenticationEntryPointSpec extends Specification {
	RedirectAppendingAuthenticationEntryPoint redirect
	HttpServletRequest request

	def setup() {
		request = new MockHttpServletRequest() {
			@Override
			String getRequestURI() {
				return "https://www.streamr.com/canvas/editor?addModule=5"
			}

			@Override
			StringBuffer getRequestURL() {
				return new StringBuffer("https://www.streamr.com/canvas/editor")
			}

			@Override
			String getQueryString() {
				return "addModule=5"
			}
		}
		redirect = new RedirectAppendingAuthenticationEntryPoint("https://www.streamr.com/login/auth") {
			@Override
			String getFullURI(String uriWithoutContextPath) {
				return "https://..."
			}
		}
	}

	def "query parameters are not lost while logging in"() {
		when:
		def url = redirect.buildRedirectUrlToLoginPage(request, null, null)

		then:
		url == "https://www.streamr.com/login/auth?redirect=https%3A%2F%2Fwww.streamr.com%2Fcanvas%2Feditor%3FaddModule%3D5"

	}
}
