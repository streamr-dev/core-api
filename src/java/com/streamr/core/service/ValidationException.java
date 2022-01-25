package com.streamr.core.service;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationException extends ApiException {
	private static final int STATUS_CODE = 422;
	private static final String CODE = "VALIDATION_ERROR";

	public ValidationException(Errors errors) {
		super(STATUS_CODE, CODE, turnToMessage(errors.getFieldErrors()));
	}

	public ValidationException(FieldError error) {
		super(STATUS_CODE, CODE, turnToMessage(Collections.singletonList(error)));
	}

	public ValidationException(String message) {
		super(STATUS_CODE, CODE, message);
	}

	private static String turnToMessage(List<FieldError> errors) {
		return "Invalid " + errors
				.stream()
				.map(ValidationException::createFieldErrorDescription)
				.collect(Collectors.joining(", "));
	}

	private static String createFieldErrorDescription(FieldError error) {
		String s = error.getField();
		if (error.getRejectedValue() != null) {
			s += ": " + error.getRejectedValue();
		}
		if (error.getCode() != null) {
			s += " (" + error.getCode() + ")";
		}
		return s;
	}
}
