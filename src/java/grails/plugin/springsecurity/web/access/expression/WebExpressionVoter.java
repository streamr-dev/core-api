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
package grails.plugin.springsecurity.web.access.expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Based on the class of the same name in Spring Security which uses the
 * package-default WebExpressionConfigAttribute.
 *
 * @author Luke Taylor
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class WebExpressionVoter implements AccessDecisionVoter<FilterInvocation> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected SecurityExpressionHandler<FilterInvocation> expressionHandler;

	public int vote(Authentication authentication, FilterInvocation fi, Collection<ConfigAttribute> attributes) {
		Assert.notNull(authentication, "authentication cannot be null");
		Assert.notNull(fi, "object cannot be null");
		Assert.notNull(attributes, "attributes cannot be null");

		log.trace("vote() Authentication {}, FilterInvocation {} ConfigAttributes {}", authentication, fi, attributes);

		WebExpressionConfigAttribute weca = findConfigAttribute(attributes);
		if (weca == null) {
			log.trace("No WebExpressionConfigAttribute found");
			return ACCESS_ABSTAIN;
		}

		EvaluationContext ctx = expressionHandler.createEvaluationContext(authentication, fi);

		return ExpressionUtils.evaluateAsBoolean(weca.getAuthorizeExpression(), ctx) ? ACCESS_GRANTED : ACCESS_DENIED;
	}

	protected WebExpressionConfigAttribute findConfigAttribute(Collection<ConfigAttribute> attributes) {
		for (ConfigAttribute attribute : attributes) {
			if (attribute instanceof WebExpressionConfigAttribute) {
				return (WebExpressionConfigAttribute)attribute;
			}
		}
		return null;
	}

	public boolean supports(ConfigAttribute attribute) {
		return attribute instanceof WebExpressionConfigAttribute;
	}

	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(FilterInvocation.class);
	}

	/**
	 * Dependency injection for the expression handler.
	 * @param handler the handler
	 */
	public void setExpressionHandler(SecurityExpressionHandler<FilterInvocation> handler) {
		expressionHandler = handler;
	}
}
