package com.unifina.security

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class RedirectAppendingAuthenticationEntryPointSpec extends Specification {

	def "redirect url is url encoded"() {
		setup:
		HttpServletRequest request = new MockHttpServletRequest()
		request.setScheme("https")
		request.setServerName("www.streamr.com")
		request.setRequestURI("/canvas/editor")
		request.setQueryString("addModule=5")

		RedirectAppendingAuthenticationEntryPoint redirect = new RedirectAppendingAuthenticationEntryPoint("https://www.streamr.com/login/auth") {
			@Override
			String getFullURI(String uriWithoutContextPath) {
				return "https://..."
			}
		}

		when:
		def url = redirect.buildRedirectUrlToLoginPage(request, null, null)

		then:
		url == "https://www.streamr.com/login/auth?redirect=https%3A%2F%2Fwww.streamr.com%2Fcanvas%2Feditor%3FaddModule%3D5"
	}

	def "redirect url is read from x-forwarded-... headers if present"() {
		setup:
		HttpServletRequest request = new MockHttpServletRequest()
		request.setScheme("http")
		request.setServerName("diipadaapa")
		request.setServerPort(8081)
		request.setRequestURI("/canvas/editor")
		request.setQueryString("addModule=5")
		request.addHeader("X-Forwarded-Proto", "https")
		request.addHeader("X-Forwarded-Host", "www.streamr.com")
		request.addHeader("X-Forwarded-Port", "443")

		RedirectAppendingAuthenticationEntryPoint redirect = new RedirectAppendingAuthenticationEntryPoint("https://www.streamr.com/login/auth") {
			@Override
			String getFullURI(String uriWithoutContextPath) {
				return "https://..."
			}
		}

		when:
		def url = redirect.buildRedirectUrlToLoginPage(request, null, null)

		then:
		url == "https://www.streamr.com/login/auth?redirect=https%3A%2F%2Fwww.streamr.com%2Fcanvas%2Feditor%3FaddModule%3D5"
	}
}
