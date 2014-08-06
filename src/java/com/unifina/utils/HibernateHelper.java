package com.unifina.utils;

import org.hibernate.proxy.HibernateProxy;

public class HibernateHelper {
	/**
	 * Utility method that tries to properly initialize the Hibernate CGLIB
	 * proxy. 
	 * 
	 * @param <T>
	 * @param maybeProxy -- the possible Hibernate generated proxy
	 * @param baseClass -- the resulting class to be cast to.
	 * @return the object of a class <T>
	 * @throws ClassCastException
	 */
	public static <T> T deproxy(Object maybeProxy, Class<T> baseClass) throws ClassCastException {
		if (maybeProxy instanceof HibernateProxy) {
			return baseClass.cast(((HibernateProxy) maybeProxy).getHibernateLazyInitializer().getImplementation());
		}
		return baseClass.cast(maybeProxy);
	}
}
