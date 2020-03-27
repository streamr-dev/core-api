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

import grails.plugin.springsecurity.InterceptedUrl;
import grails.util.GrailsUtil;
import grails.util.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public abstract class AbstractFilterInvocationDefinition implements FilterInvocationSecurityMetadataSource, InitializingBean {

	protected static final Collection<ConfigAttribute> DENY = Collections.singletonList((ConfigAttribute)new SecurityConfig("_DENY_"));

	protected boolean rejectIfNoRule;
	protected RoleVoter roleVoter;
	protected AuthenticatedVoter authenticatedVoter;
	protected final List<InterceptedUrl> compiled = new CopyOnWriteArrayList<InterceptedUrl>();
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	protected AntPathMatcher urlMatcher = new AntPathMatcher();
	protected boolean initialized;
	protected boolean grails23Plus;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Allows subclasses to be externally reset.
	 * @throws Exception
	 */
	public void reset() throws Exception {
		// override if necessary
	}

	public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
		Assert.notNull(object, "Object must be a FilterInvocation");
		Assert.isTrue(supports(object.getClass()), "Object must be a FilterInvocation");

		FilterInvocation filterInvocation = (FilterInvocation)object;

		String url = determineUrl(filterInvocation);
		log.trace("getAttributes(): url is {} for FilterInvocation {}", url, filterInvocation);

		Collection<ConfigAttribute> configAttributes;
		try {
			configAttributes = findConfigAttributes(url, filterInvocation.getRequest().getMethod());
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		if ((configAttributes == null || configAttributes.isEmpty()) && rejectIfNoRule) {
			log.trace("Returning DENY, rejectIfNoRule is true and no ConfigAttributes");
			// return something that cannot be valid; this will cause the voters to abstain or deny
			return DENY;
		}

		log.trace("ConfigAttributes are {}", configAttributes);
		return configAttributes;
	}

	protected String determineUrl(final FilterInvocation filterInvocation) {
		return lowercaseAndStripQuerystring(calculateUri(filterInvocation.getHttpRequest()));
	}

	protected boolean stopAtFirstMatch() {
		return false;
	}

	// for testing
	public InterceptedUrl getInterceptedUrl(final String url, final HttpMethod httpMethod) throws Exception {

		initialize();

		for (InterceptedUrl iu : compiled) {
			if (iu.getHttpMethod() == httpMethod && iu.getPattern().equals(url)) {
				return iu;
			}
		}

		return null;
	}

	protected Collection<ConfigAttribute> findConfigAttributes(final String url, final String requestMethod) throws Exception {

		initialize();

		Collection<ConfigAttribute> configAttributes = null;
		String configAttributePattern = null;

		boolean stopAtFirstMatch = stopAtFirstMatch();
		for (InterceptedUrl iu : compiled) {

			if (iu.getHttpMethod() != null && requestMethod != null && iu.getHttpMethod() != HttpMethod.valueOf(requestMethod)) {
				if (log.isDebugEnabled()) {
					log.debug("Request '{} {}' doesn't match '{} {}'", new Object[] { requestMethod, url, iu.getHttpMethod(), iu.getPattern() });
				}
				continue;
			}

			if (urlMatcher.match(iu.getPattern(), url)) {
				if (configAttributes == null || urlMatcher.match(configAttributePattern, iu.getPattern())) {
					configAttributes = iu.getConfigAttributes();
					configAttributePattern = iu.getPattern();
					if (log.isTraceEnabled()) {
						log.trace("new candidate for '{}': '{}':{}", new Object[] { url, iu.getPattern(), configAttributes });
					}
					if (stopAtFirstMatch) {
						break;
					}
				}
			}
		}

		if (log.isTraceEnabled()) {
			if (configAttributes == null) {
				log.trace("no config for '{}'", url);
			}
			else {
				log.trace("config for '{}' is '{}':{}", new Object[] { url, configAttributePattern, configAttributes });
			}
		}

		return configAttributes;
	}

	protected void initialize() throws Exception {
		// override if necessary
	}

	public boolean supports(Class<?> clazz) {
		return FilterInvocation.class.isAssignableFrom(clazz);
	}

	public Collection<ConfigAttribute> getAllConfigAttributes() {
		try {
			initialize();
		}
		catch (Exception e) {
			GrailsUtil.deepSanitize(e);
			log.error(e.getMessage(), e);
		}

		Collection<ConfigAttribute> all = new LinkedHashSet<ConfigAttribute>();
		for (InterceptedUrl iu : compiled) {
			all.addAll(iu.getConfigAttributes());
		}
		return Collections.unmodifiableCollection(all);
	}

	protected String calculateUri(final HttpServletRequest request) {
		String url = request.getRequestURI().substring(request.getContextPath().length());
		int semicolonIndex = url.indexOf(";");
		return semicolonIndex == -1 ? url : url.substring(0, semicolonIndex);
	}

	protected String lowercaseAndStripQuerystring(final String url) {

		String fixed = url.toLowerCase();

		int firstQuestionMarkIndex = fixed.indexOf("?");
		if (firstQuestionMarkIndex != -1) {
			fixed = fixed.substring(0, firstQuestionMarkIndex);
		}

		return fixed;
	}

	protected AntPathMatcher getUrlMatcher() {
		return urlMatcher;
	}

	/**
	 * For debugging.
	 * @return an unmodifiable map of {@link AnnotationFilterInvocationDefinition}ConfigAttributeDefinition
	 * keyed by compiled patterns
	 */
	public List<InterceptedUrl> getConfigAttributeMap() {
		return Collections.unmodifiableList(compiled);
	}

	// fixes extra spaces, trailing commas, etc.
	protected List<String> split(final String value) {
		if (!value.startsWith("ROLE_") && !value.startsWith("IS_")) {
			// an expression
			return Collections.singletonList(value);
		}

		String[] parts = StringUtils.commaDelimitedListToStringArray(value);
		List<String> cleaned = new ArrayList<String>();
		for (String part : parts) {
			part = part.trim();
			if (part.length() > 0) {
				cleaned.add(part);
			}
		}
		return cleaned;
	}

	protected void compileAndStoreMapping(InterceptedUrl iu) {
		String pattern = iu.getPattern();
		HttpMethod method = iu.getHttpMethod();

		String key = pattern.toLowerCase();

		Collection<ConfigAttribute> configAttributes = iu.getConfigAttributes();

		InterceptedUrl replaced = storeMapping(key, method, Collections.unmodifiableCollection(configAttributes));
		if (replaced != null) {
			log.warn("replaced rule for '{}' with roles {} with roles {}", new Object[] { key, replaced.getConfigAttributes(), configAttributes });
		}
	}

	protected InterceptedUrl storeMapping(final String pattern, final HttpMethod method,
			final Collection<ConfigAttribute> configAttributes) {

		InterceptedUrl existing = null;
		for (InterceptedUrl iu : compiled) {
			if (iu.getPattern().equals(pattern) && iu.getHttpMethod() == method) {
				existing = iu;
				break;
			}
		}

		if (existing != null) {
			log.trace("Replacing existing mapping {}", existing);
			compiled.remove(existing);
		}

		InterceptedUrl mapping = new InterceptedUrl(pattern, method, configAttributes);
		compiled.add(mapping);
		log.trace("Stored mapping {} for pattern '{}', HttpMethod {}, ConfigAttributes {}",
				new Object[] { mapping, pattern, method, configAttributes });

		return existing;
	}

	protected void resetConfigs() {
		compiled.clear();
	}

	/**
	 * For admin/debugging - find all config attributes that apply to the specified URL (doesn't consider request method restrictions).
	 * @param url the URL
	 * @return matching attributes
	 */
	public Collection<ConfigAttribute> findMatchingAttributes(final String url) {
		for (InterceptedUrl iu : compiled) {
			if (urlMatcher.match(iu.getPattern(), url)) {
				return iu.getConfigAttributes();
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Dependency injection for whether to reject if there's no matching rule.
	 * @param reject if true, reject access unless there's a pattern for the specified resource
	 */
	public void setRejectIfNoRule(final boolean reject) {
		rejectIfNoRule = reject;
	}

	public void afterPropertiesSet() {
		String version = Metadata.getCurrent().getGrailsVersion();
		grails23Plus = !version.startsWith("2.0") && !version.startsWith("2.1") && !version.startsWith("2.2");
	}
}
