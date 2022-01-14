package com.unifina.domain;

import java.math.BigInteger;

public enum StreamPermission {
	EDIT,
	DELETE,
	PUBLISH,
	SUBSCRIBE,
	GRANT;

	public BigInteger toBigInteger() {
		return BigInteger.valueOf(ordinal());
	}
}
