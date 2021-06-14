package com.unifina.service;
import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;
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
