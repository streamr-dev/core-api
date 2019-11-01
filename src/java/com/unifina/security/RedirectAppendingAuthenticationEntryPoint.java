package com.unifina.security;

import com.unifina.controller.security.LoginRedirectValidator;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.mapping.LinkGenerator;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.RedirectUrlBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * An extension to LoginUrlAuthenticationEntryPoint, which appends a 'redirect' query parameter
 * when trying to access a protected page and redirecting to the login view.
 */
public class RedirectAppendingAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	private static final Logger log = Logger.getLogger(RedirectAppendingAuthenticationEntryPoint.class);
	private static final String REDIRECT_PARAM_NAME = "redirect";

	private LinkGenerator linkGenerator;
	private String defaultRedirectURI;

	public RedirectAppendingAuthenticationEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	private String getOriginalRequestUrl(HttpServletRequest request) {
		RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
		urlBuilder.setScheme(request.getHeader("x-forwarded-proto") != null ?
				request.getHeader("x-forwarded-proto") :
				request.getScheme()
		);
		urlBuilder.setServerName(request.getHeader("x-forwarded-host") != null ?
				request.getHeader("x-forwarded-host") :
				request.getServerName())
		;
		urlBuilder.setPort(request.getHeader("x-forwarded-port") != null ?
				Integer.parseInt(request.getHeader("x-forwarded-port")) :
				getPortResolver().getServerPort(request)
		);
		urlBuilder.setPathInfo(request.getRequestURI());
		urlBuilder.setQuery(request.getQueryString()); // is null safe
		return urlBuilder.getUrl();
	}

	@Override
	protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
		String loginPageUrl = super.buildRedirectUrlToLoginPage(request, response, authException);

		// If we are looking for the base url or the default redirect, don't add the redirect query param
		if (request.getRequestURI().equals(getFullURI("/"))
				|| request.getRequestURI().equals(getFullURI(defaultRedirectURI))) {
			return loginPageUrl;
		} else {
			String redirectUrl = getOriginalRequestUrl(request);
			if (LoginRedirectValidator.isValid(redirectUrl)) {
				return String.format("%s?%s=%s", loginPageUrl, REDIRECT_PARAM_NAME, urlencode(redirectUrl));
			} else {
				log.warn("Redirect url rejected: " + request.getRequestURL());
				return loginPageUrl;
			}
		}
	}

	private String urlencode(final String s) {
		try {
			return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			log.error("error while url encoding parameter", e);
		}
		return s;
	}

	public String getRedirectParameterName() {
		return REDIRECT_PARAM_NAME;
	}

	String getFullURI(String uriWithoutContextPath) {
		Map<String, String> linkGeneratorArgs = new HashMap<>();
		linkGeneratorArgs.put("uri", uriWithoutContextPath);
		return linkGenerator.link(linkGeneratorArgs);
	}

	public void setLinkGenerator(LinkGenerator linkGenerator) {
		this.linkGenerator = linkGenerator;
	}

	public void setDefaultRedirectURI(String defaultRedirectURI) {
		this.defaultRedirectURI = defaultRedirectURI;
	}
}
