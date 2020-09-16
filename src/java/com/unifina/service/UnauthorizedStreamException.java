package com.unifina.service;

import com.unifina.domain.Canvas;
import com.unifina.domain.Stream;
import com.unifina.domain.User;

public class UnauthorizedStreamException extends RuntimeException {
	public Canvas canvas;
	public Stream stream;
	public User user;
	public UnauthorizedStreamException(Canvas canvas, Stream stream, User user) {
		super("User " + user.getName() + " (id " + user.getId() + ") doesn't have the permissions to read stream " + stream.getId() + " on canvas " + canvas.getId());
		this.canvas = canvas;
		this.stream = stream;
		this.user = user;
	}
}
