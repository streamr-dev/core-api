package com.unifina.exceptions;

public class UserCreationFailedException extends RuntimeException {
	public UserCreationFailedException(String name){
        super(name);
    }
}
