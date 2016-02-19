package com.unifina.exceptions;

/**
 * Created by henripihkala on 16/02/16.
 */
public class UnexpectedApiResponseException extends RuntimeException {
	public UnexpectedApiResponseException(String message) {
		super(message);
	}
}
