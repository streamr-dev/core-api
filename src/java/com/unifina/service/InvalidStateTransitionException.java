package com.unifina.service;

public class InvalidStateTransitionException extends ApiException {
	public <T extends Enum> InvalidStateTransitionException(T fromState, T toState) {
		super(409, "INVALID_STATE_TRANSITION", formMessage(fromState.toString(), toState.toString()));
	}

	private static String formMessage(String fromState, String toState) {
		return String.format("Invalid transition %s -> %s", fromState, toState);
	}
}
