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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;
import grails.plugin.springsecurity.SpringSecurityUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Extends the default {@link UsernamePasswordAuthenticationFilter} to store the
 * last attempted login username in the session under the 'SPRING_SECURITY_LAST_USERNAME'
 * key if storeLastUsername is true.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	protected Boolean storeLastUsername;

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

		if (storeLastUsername) {
			// Place the last username attempted into HttpSession for views
			HttpSession session = request.getSession(false);
			if (session == null && getAllowSessionCreation()) {
				session = request.getSession();
			}

			if (session != null) {
				String username = obtainUsername(request);
				if (username == null) {
					username = "";
				}
				username = username.trim();
				session.setAttribute(SpringSecurityUtils.SPRING_SECURITY_LAST_USERNAME_KEY, username);
			}
		}

		return super.attemptAuthentication(request, response);
	}

	/**
	 * Whether to store the last attempted username in the session.
	 * @param storeLastUsername store if true
	 */
	public void setStoreLastUsername(Boolean storeLastUsername) {
		this.storeLastUsername = storeLastUsername;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Assert.notNull(storeLastUsername, "storeLastUsername must be set");
	}
}
