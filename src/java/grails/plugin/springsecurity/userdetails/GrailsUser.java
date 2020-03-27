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
package grails.plugin.springsecurity.userdetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Extends the default Spring Security user class to contain the ID for efficient lookup
 * of the domain class from the Authentication.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsUser extends User {

	private static final long serialVersionUID = 1;

	private final Object id;

	/**
	 * Constructor.
	 *
	 * @param username the username presented to the
	 *        <code>DaoAuthenticationProvider</code>
	 * @param password the password that should be presented to the
	 *        <code>DaoAuthenticationProvider</code>
	 * @param enabled set to <code>true</code> if the user is enabled
	 * @param accountNonExpired set to <code>true</code> if the account has not expired
	 * @param credentialsNonExpired set to <code>true</code> if the credentials have not expired
	 * @param accountNonLocked set to <code>true</code> if the account is not locked
	 * @param authorities the authorities that should be granted to the caller if they
	 *        presented the correct username and password and the user is enabled. Not null.
	 * @param id the id of the domain class instance used to populate this
	 */
	public GrailsUser(String username, String password, boolean enabled, boolean accountNonExpired,
	                  boolean credentialsNonExpired, boolean accountNonLocked,
	                  Collection<GrantedAuthority> authorities, Object id) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, authorities);
		this.id = id;
	}

	/**
	 * Get the id.
	 * @return the id
	 */
	public Object getId() {
		return id;
	}
}
