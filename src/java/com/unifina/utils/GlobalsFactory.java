package com.unifina.utils;

import com.unifina.domain.security.SecUser;
import grails.plugin.springsecurity.SpringSecurityService;
import grails.util.Environment;
import grails.util.Holders;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Map;

/**
 * This class provides methods to create the Globals instance that serves
 * as a holder/context for a certain SignalPath run. The returned object must
 * be explicitly destroyed when it is no longer needed. The default Globals objects
 * destroys itself on the DataSource stop event, but if you do not run the DataSource
 * (saving, loading etc.) you must destroy the Globals object explicitly.
 * 
 * @author Henri
 *
 */
public class GlobalsFactory {
	/**
	 * Instantiate a Globals object. If user == null attempt to fetch currently logged-in user.
	 */
	public static Globals createInstance(Map signalPathContext, SecUser user) {
		if (user == null) {
			SpringSecurityService springSecurityService = Holders.getApplicationContext().getBean(SpringSecurityService.class);
			if (springSecurityService != null) {
				user = (SecUser) springSecurityService.getCurrentUser();
			}
		}
		return new Globals(signalPathContext, user);
	}
}
