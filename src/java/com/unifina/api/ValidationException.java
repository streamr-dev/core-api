package com.unifina.api;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class ValidationException extends ApiException {

	public ValidationException(Errors errors) {
		super(422, "VALIDATION_ERROR", turnToMessage(errors));
	}

	private static String turnToMessage(Errors errors) {
		String msg = "Validation failed for fields:\n";
		for (FieldError error : errors.getFieldErrors()) {
			msg += error.getField() + " (" + error.getCode() + ")\n";
		}
		return msg;
	}
}
