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
package grails.plugin.springsecurity;

import grails.util.Environment;
import groovy.lang.GroovyClassLoader;
import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public final class SpringSecurityUtils {

	private static final Logger LOG = LoggerFactory.getLogger(SpringSecurityUtils.class);

	private static ConfigObject _securityConfig;
	private static GrailsApplication application;

	private SpringSecurityUtils() {
		// static only
	}

	/**
	 * Parse and load the security configuration.
	 * @return the configuration
	 */
	public static synchronized ConfigObject getSecurityConfig() {
		if (_securityConfig == null) {
			LOG.trace("Building security config since there is no cached config");
			reloadSecurityConfig();
		}

		return _securityConfig;
	}

	/**
	 * For testing only.
	 * @param config the config
	 */
	public static void setSecurityConfig(ConfigObject config) {
		_securityConfig = config;
	}


	/**
	 * Force a reload of the security configuration.
	 */
	public static void reloadSecurityConfig() {
		mergeConfig(ReflectionUtils.getSecurityConfig(), "DefaultSecurityConfig");
		LOG.trace("reloaded security config");
	}

	/**
	 * Merge in a secondary config (provided by a plugin as defaults) into the main config.
	 * @param currentConfig the current configuration
	 * @param className the name of the config class to load
	 */
	private static void mergeConfig(final ConfigObject currentConfig, final String className) {
		GroovyClassLoader classLoader = new GroovyClassLoader(SpringSecurityUtils.class.getClassLoader());
		ConfigSlurper slurper = new ConfigSlurper(Environment.getCurrent().getName());
		ConfigObject secondaryConfig;
		try {
			secondaryConfig = slurper.parse(classLoader.loadClass(className));
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		_securityConfig = mergeConfig(currentConfig, (ConfigObject)secondaryConfig.getProperty("security"));
		ReflectionUtils.setSecurityConfig(_securityConfig);
	}

	/**
	 * Merge two configs together. The order is important; if <code>secondary</code> is not null then
	 * start with that and merge the main config on top of that. This lets the <code>secondary</code>
	 * config act as default values but let user-supplied values in the main config override them.
	 *
	 * @param currentConfig the main config, starting from Config.groovy
	 * @param secondary new default values
	 * @return the merged configs
	 */
	private static ConfigObject mergeConfig(final ConfigObject currentConfig, final ConfigObject secondary) {
		ConfigObject config = new ConfigObject();
		if (secondary == null) {
			if (currentConfig != null) {
				config.putAll(currentConfig);
			}
		}
		else {
			if (currentConfig == null) {
				config.putAll(secondary);
			}
			else {
				config.putAll(secondary.merge(currentConfig));
			}
		}
		return config;
	}
}
