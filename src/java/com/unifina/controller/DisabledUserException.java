package com.unifina.controller;

public class DisabledUserException extends RuntimeException {
	public DisabledUserException(String message) {
		super(message);
	}
}
