package com.unifina.security;

import com.unifina.controller.security.LoginRedirectValidator;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.mapping.LinkGenerator;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * An AjaxAwareAuthenticationEntryPoint which appends a 'redirect' query parameter
 * when trying to access a protected page and redirecting to the login view.
 */
public class RedirectAppendingAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	private static final Logger log = Logger.getLogger(RedirectAppendingAuthenticationEntryPoint.class);

	private LinkGenerator linkGenerator;
	private String defaultRedirectURI;

	public RedirectAppendingAuthenticationEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	@Override
	protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
		String url = super.buildRedirectUrlToLoginPage(request, response, authException);

		// If we are looking for the base url or the default redirect, don't add the redirect query param
		if (request.getRequestURI().equals(getFullURI("/"))
				|| request.getRequestURI().equals(getFullURI(defaultRedirectURI))) {
			return url;
		} else if (LoginRedirectValidator.isValid(request.getRequestURL().toString())){
			return url + "?redirect="+request.getRequestURL();
		} else {
			log.info("Redirect url rejected: "+request.getRequestURL());
			return url;
		}
	}

	private String getFullURI(String uriWithoutContextPath) {
		Map<String, String> linkGeneratorArgs = new HashMap<>();
		linkGeneratorArgs.put("uri", uriWithoutContextPath);
		return linkGenerator.link(linkGeneratorArgs);
	}

	public LinkGenerator getLinkGenerator() {
		return linkGenerator;
	}

	public void setLinkGenerator(LinkGenerator linkGenerator) {
		this.linkGenerator = linkGenerator;
	}

	public String getDefaultRedirectURI() {
		return defaultRedirectURI;
	}

	public void setDefaultRedirectURI(String defaultRedirectURI) {
		this.defaultRedirectURI = defaultRedirectURI;
	}
}
