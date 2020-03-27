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
package grails.plugin.springsecurity.annotation

import java.lang.annotation.*

/**
 * Annotation for Controllers at the class level or per-action, defining what roles
 * are required for the entire controller or action.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@interface Secured {

	/**
	 * Default value for httpMethod().
	 */
	String ANY_METHOD = 'ANY'

	/**
	 * Defines the security configuration attributes (e.g. ROLE_USER, ROLE_ADMIN, IS_AUTHENTICATED_REMEMBERED, etc.)
	 * @return the names of the roles, expressions, and tokens
	 */
	String[] value() default []

	/**
	 * Optional attribute to specify the HTTP method required.
	 * @return the method
	 */
	String httpMethod() default 'ANY'

	/**
	 * Optional attribute to specify a closure that will be evaluated to decide if access should be allowed.
	 * @return the closure class
	 */
	Class<?> closure() default Secured
}
