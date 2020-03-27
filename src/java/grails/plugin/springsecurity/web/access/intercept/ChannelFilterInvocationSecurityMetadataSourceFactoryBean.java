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
package grails.plugin.springsecurity.web.access.intercept;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Factory bean that builds a {@link FilterInvocationSecurityMetadataSource} for channel security.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class ChannelFilterInvocationSecurityMetadataSourceFactoryBean
       implements FactoryBean<FilterInvocationSecurityMetadataSource>, InitializingBean {

	protected static final Collection<String> SUPPORTED = Arrays.asList(
			"ANY_CHANNEL", "REQUIRES_SECURE_CHANNEL", "REQUIRES_INSECURE_CHANNEL");
	protected AntPathMatcher urlMatcher = new AntPathMatcher();
	protected Map<String, String> definition;
	protected DefaultFilterInvocationSecurityMetadataSource source;

	public FilterInvocationSecurityMetadataSource getObject() {
		return source;
	}

	public Class<DefaultFilterInvocationSecurityMetadataSource> getObjectType() {
		return DefaultFilterInvocationSecurityMetadataSource.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() {
		Assert.notNull(definition, "definition map is required");
		Assert.notNull(urlMatcher, "urlMatcher is required");

		source = new DefaultFilterInvocationSecurityMetadataSource(buildMap());
	}

	protected LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> buildMap() {
		LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
		for (Map.Entry<String, String> entry : definition.entrySet()) {
			String value = entry.getValue();
			if (value == null) {
				throw new IllegalArgumentException("The rule for URL '" + value + "' cannot be null");
			}
			value = value.trim();

			if (!SUPPORTED.contains(value) ) {
				throw new IllegalArgumentException("The rule for URL '" + value +
						"' must be one of REQUIRES_SECURE_CHANNEL, REQUIRES_INSECURE_CHANNEL, or ANY_CHANNEL");
			}

			map.put(new AntPathRequestMatcher(entry.getKey()), SecurityConfig.createList(value));
		}
		return map;
	}

	/**
	 * Dependency injection for the definition map.
	 *
	 * @param d keys are URL patterns, values are ANY_CHANNEL, REQUIRES_SECURE_CHANNEL, or REQUIRES_INSECURE_CHANNEL
	 */
	public void setDefinition(Map<String, String> d) {
		definition = d;
	}
}
