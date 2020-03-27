/* Copyright 2013-2015 the original author or authors.
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
package grails.plugin.springsecurity.authentication;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsAnonymousAuthenticationToken extends AnonymousAuthenticationToken {

	// TODO use this
	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	public static final String USERNAME = "__grails.anonymous.user__";
	public static final String PASSWORD = "";
	public static final String ROLE_NAME = "ROLE_ANONYMOUS";
	public static final GrantedAuthority ROLE = new SimpleGrantedAuthority(ROLE_NAME);
	public static final List<GrantedAuthority> ROLES = Collections.singletonList(ROLE);
	public static final UserDetails USER_DETAILS = new User(USERNAME, PASSWORD, false, false, false, false, ROLES);

	/**
	 * Constructor.
	 */
	public GrailsAnonymousAuthenticationToken(String key, Object details) {
		super(key, USER_DETAILS, ROLES);
		setDetails(details);
	}
}
