package com.unifina.exceptions;

import com.unifina.domain.data.Stream;
import com.unifina.domain.security.SecUser;
import com.unifina.domain.signalpath.Canvas;

public class UnauthorizedStreamException extends RuntimeException {
	public Canvas canvas;
	public Stream stream;
	public SecUser user;
	public UnauthorizedStreamException(Canvas canvas, Stream stream, SecUser user) {
		super("User " + user.getName() + " (id " + user.getId() + ") doesn't have the permissions to read stream " + stream.getId() + " on canvas " + canvas.getId());
		this.canvas = canvas;
		this.stream = stream;
		this.user = user;
	}
}
