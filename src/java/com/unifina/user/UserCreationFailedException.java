package com.unifina.user;

public class UserCreationFailedException extends RuntimeException {
	public UserCreationFailedException(String name){
        super(name);
    }
}
