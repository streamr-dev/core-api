/* Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springsecurity.web.authentication;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.util.Assert;
import grails.plugin.springsecurity.SpringSecurityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Ajax-aware failure handler that detects failed Ajax logins and redirects to the appropriate URL.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class AjaxAwareAuthenticationFailureHandler extends ExceptionMappingAuthenticationFailureHandler implements InitializingBean {

	protected String ajaxAuthenticationFailureUrl;

	@Override
	public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException exception) throws IOException, ServletException {

		if (SpringSecurityUtils.isAjax(request)) {
			saveException(request, exception);
			getRedirectStrategy().sendRedirect(request, response, ajaxAuthenticationFailureUrl);
		}
		else {
			super.onAuthenticationFailure(request, response, exception);
		}
	}

	/**
	 * Dependency injection for the Ajax auth fail url.
	 * @param url the url
	 */
	public void setAjaxAuthenticationFailureUrl(final String url) {
		ajaxAuthenticationFailureUrl = url;
	}

	public void afterPropertiesSet() {
		Assert.notNull(ajaxAuthenticationFailureUrl, "ajaxAuthenticationFailureUrl is required");
	}
}
