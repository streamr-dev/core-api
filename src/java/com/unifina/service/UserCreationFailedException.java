package com.unifina.service;

public class UserCreationFailedException extends RuntimeException {
	public UserCreationFailedException(String name){
        super(name);
    }
}
