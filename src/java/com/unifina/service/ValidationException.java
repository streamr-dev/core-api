package com.unifina.service;
import java.util.List;
import java.util.Collections;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

public class ValidationException extends RuntimeException {

	public ValidationException() {}

	public ValidationException(Errors errors) {
		super(turnToMessage(errors.getFieldErrors()));
	}

	public ValidationException(FieldError error) {
		super(turnToMessage(Collections.singletonList(error)));
	}

	public ValidationException(String message) {
		super(message);
	}

	private static String turnToMessage(List<FieldError> errors) {
		String msg = "Validation failed for fields:\n";
		for (FieldError error : errors) {
			msg += error.getField() + " (" + error.getCode() + ")\n";
		}
		return msg;
	}
}
