package com.unifina.utils

/**
 * Throw when {@link ApplicationConfig#getString(String)} returns invalid value.
 */
class ApplicationConfigException extends NullPointerException {
	ApplicationConfigException(String message) {
		super(message)
	}
}
