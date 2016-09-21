package com.unifina.api;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class ValidationException extends RuntimeException {

	public ValidationException() {}

	public ValidationException(Errors errors) {
		super(turnToMessage(errors));
	}

	private static String turnToMessage(Errors errors) {
		String msg = "Validation failed for fields:\n";
		for (FieldError error : errors.getFieldErrors()) {
			msg += error.getField() + " (" + error.getCode() + ")\n";
		}
		return msg;
	}
}
