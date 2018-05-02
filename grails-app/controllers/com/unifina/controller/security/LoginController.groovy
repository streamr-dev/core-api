package com.unifina.controller.security

import com.unifina.security.RedirectAppendingAuthenticationEntryPoint
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.security.web.savedrequest.SavedRequest

import javax.servlet.http.HttpServletResponse

@Secured(["permitAll"])
class LoginController {

	/**
	 * Dependency injection for the authenticationTrustResolver.
	 */
	def authenticationTrustResolver

	/**
	 * Dependency injection for the springSecurityService.
	 */
	def springSecurityService

	AuthenticationEntryPoint authenticationEntryPoint
	RequestCache requestCache

	/**
	 * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
	 */
	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
		}
		else {
			redirect action: 'auth', params: params
		}
	}

	/**
	 * Show the login page.
	 */
	def auth = {

		def config = SpringSecurityUtils.securityConfig

		if (springSecurityService.isLoggedIn()) {
			redirect uri: config.successHandler.defaultTargetUrl
			return
		}

		String view = 'auth'
		String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"

		render view: view, model: [postUrl            : postUrl,
								   rememberMeParameter: config.rememberMe.parameter]
	}

	/**
	 * The redirect action for Ajax requests.
	 */
	def authAjax = {
		response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
		response.sendError HttpServletResponse.SC_UNAUTHORIZED
	}

	/**
	 * Show denied page. We come here if access to a page is denied.
	 * Note: ALSO if we have a remember me cookie, but the page requires IS_AUTHENTICATED_FULLY.
	 * In that case, we redirect to the login view.
	 */
	def denied = {
		if (springSecurityService.isLoggedIn() &&
				authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
			/**
			 * User has the cookie, but the page is guarded with IS_AUTHENTICATED_FULLY.
			 * AjaxAwareAccessDeniedHandler saves the original request to requestCache. Pick it up
			 * from there to find out where the user was going, and add the redirect query param.
 			 */
			SavedRequest savedRequest = requestCache.getRequest(request, response)
			if (authenticationEntryPoint instanceof RedirectAppendingAuthenticationEntryPoint) {
				Map redirectParams = [:]
				redirectParams.put(authenticationEntryPoint.getRedirectParameterName(), savedRequest.getRedirectUrl())
				redirect action: 'full', params: [redirect: savedRequest.getRedirectUrl()]
			} else {
				throw new IllegalStateException("Whoa! 'authenticationEntryPoint' bean is not an RedirectAppendingAuthenticationEntryPoint! I wasn't expecting that!")
			}
		}
		else if (!springSecurityService.isLoggedIn()) {
			redirect action: 'auth'
		}
	}

	/**
	 * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
	 */
	def full = {
		def config = SpringSecurityUtils.securityConfig
		render view: 'auth', params: params,
			model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
			        postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
	}

	/**
	 * Callback after a failed login. Redirects to the auth page with a warning message.
	 */
	def authfail = {

		def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
		String msg = ''
		def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
		if (exception) {
			if (exception instanceof AccountExpiredException) {
				msg = g.message(code: "springSecurity.errors.login.expired")
			}
			else if (exception instanceof CredentialsExpiredException) {
				msg = g.message(code: "springSecurity.errors.login.passwordExpired")
			}
			else if (exception instanceof DisabledException) {
				msg = g.message(code: "springSecurity.errors.login.disabled")
			}
			else if (exception instanceof LockedException) {
				msg = g.message(code: "springSecurity.errors.login.locked")
			}
			else {
				msg = g.message(code: "springSecurity.errors.login.fail")
			}
		}

		if (springSecurityService.isAjax(request)) {
			render([error: msg] as JSON)
		}
		else {
			flash.message = msg
			redirect action: 'auth', params: params
		}
	}

	/**
	 * The Ajax success redirect url.
	 */
	def ajaxSuccess = {
		render([success: true, user: springSecurityService.currentUser.toMap()] as JSON)
	}

	/**
	 * The Ajax denied redirect url.
	 */
	def ajaxDenied = {
		render([error: 'access denied'] as JSON)
	}

	def loginForm() {
		def config = SpringSecurityUtils.securityConfig
		String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
		render(template:"form", model: [postUrl: postUrl, rememberMeParameter: config.rememberMe.parameter])
	}
}
