package com.unifina.utils

import grails.compiler.GrailsCompileStatic
import grails.util.Holders

@GrailsCompileStatic
final class ApplicationConfig {
	private ApplicationConfig() {}

	static String getString(String key) {
		return MapTraversal.getString(Holders.getConfig(), key)
	}
}
