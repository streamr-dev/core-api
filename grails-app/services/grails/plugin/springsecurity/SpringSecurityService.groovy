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
package grails.plugin.springsecurity

import com.unifina.domain.security.SecUser
import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.util.Assert

/**
 * Utility methods.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class SpringSecurityService {

	/** dependency injection for authenticationTrustResolver */
	def authenticationTrustResolver

	/** dependency injection for grailsApplication */
	def grailsApplication

	/** dependency injection for the password encoder */
	def passwordEncoder

	/**
	 * Get the currently logged in user's principal. If not authenticated and the
	 * AnonymousAuthenticationFilter is active (true by default) then the anonymous
	 * user's name will be returned ('anonymousUser' unless overridden).
	 *
	 * @return the principal
	 */
	def getPrincipal() { getAuthentication()?.principal }

	/**
	 * Get the currently logged in user's <code>Authentication</code>. If not authenticated
	 * and the AnonymousAuthenticationFilter is active (true by default) then the anonymous
	 * user's auth will be returned (AnonymousAuthenticationToken with username 'anonymousUser'
	 * unless overridden).
	 *
	 * @return the authentication
	 */
	Authentication getAuthentication() { SCH.context?.authentication }

	/**
	 * Get the domain class instance associated with the current authentication.
	 * @return the user
	 */
	def getCurrentUser() {
		if (!isLoggedIn()) {
			return null
		}

		def User = new SecUser()

		if (principal instanceof GrailsUser) {
			User.get principal.id
		}
		else {
			User.createCriteria().get {
				eq "username", principal.username
				cache true
			}
		}
	}

	def getCurrentUserId() {
		def principal = getPrincipal()
		principal instanceof GrailsUser ? principal.id : null
	}

	/**
	 * Get a proxy for the domain class instance associated with the current authentication. Use this when you
	 * want the user only for its id, e.g. as a proxy for the foreign key in queries like "CreditCard.findAllByUser(user)"
	 *
	 * @return the proxy
	 */
	def loadCurrentUser() {
		if (!isLoggedIn()) {
			return null
		}

		// load() requires an id, so this only works if there's an id property in the principal
		Assert.isInstanceOf GrailsUser, principal

		new SecUser().load(currentUserId)
	}

	/**
	 * Encode the password using the configured PasswordEncoder.
	 */
	String encodePassword(String password, String salt = null) {
		salt = null // bcrypt has no salt
		return passwordEncoder.encodePassword(password, salt)
	}

	/**
	 * Quick check to see if the current user is logged in.
	 * @return <code>true</code> if the authenticated and not anonymous
	 */
	boolean isLoggedIn() {
		def authentication = SCH.context.authentication
		authentication && !authenticationTrustResolver.isAnonymous(authentication)
	}
}
