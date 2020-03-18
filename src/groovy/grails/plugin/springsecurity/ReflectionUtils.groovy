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

import grails.util.Holders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * Helper methods in Groovy.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class ReflectionUtils {

	private static final Logger log = LoggerFactory.getLogger(this)

	private ReflectionUtils() {
		// static only
	}

	static ConfigObject getSecurityConfig() {
		def grailsConfig = Holders.grailsApplication.config
		return grailsConfig.grails.plugin.springsecurity
	}

	static void setSecurityConfig(ConfigObject c) {
		Holders.grailsApplication.config.grails.plugin.springsecurity = c
	}
}
