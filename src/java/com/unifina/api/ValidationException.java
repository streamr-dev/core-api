package com.unifina.api;

import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

public class ValidationException extends RuntimeException {

	public ValidationException(Errors errors) {
		super(turnToMessage(errors));
	}

	private static String turnToMessage(Errors errors) {
		String msg = "";
		for (ObjectError error : errors.getAllErrors()) {
			msg += error.toString() + "\n";
		}
		return msg;
	}
}
