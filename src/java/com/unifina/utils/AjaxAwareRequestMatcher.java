package com.unifina.utils;

import grails.plugin.springsecurity.SpringSecurityUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

/**
 * RequestMatcher which does not match ajax requests.
 * Can be used with a RequestCache to stop post-login redirects to URLs which originally were ajax requests!
 */
public class AjaxAwareRequestMatcher implements RequestMatcher {

	@Override
	public boolean matches(HttpServletRequest request) {
		// TODO: isAjax checks for X-Requested-With header, which is a convention, not a standard. It will not detect requests made using the new fetch function.
		return request.getMethod().equals("GET") && !SpringSecurityUtils.isAjax(request);
	}
}
