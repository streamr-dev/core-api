package com.unifina.api;

import grails.validation.ValidationErrors;
import org.springframework.validation.ObjectError;

public class ValidationException extends ApiException {

	public ValidationException(ValidationErrors errors) {
		super(422, "VALIDATION_ERROR", turnToMessage(errors));
	}

	private static String turnToMessage(ValidationErrors errors) {
		String msg = "";
		for (ObjectError error : errors.getAllErrors()) {
			msg += error.toString() + "\n";
		}
		return msg;
	}
}
