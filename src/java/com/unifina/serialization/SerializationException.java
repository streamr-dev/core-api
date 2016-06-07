package com.unifina.serialization;

public class SerializationException extends RuntimeException {

	public SerializationException(String message) {
		super(message);
	}

	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
